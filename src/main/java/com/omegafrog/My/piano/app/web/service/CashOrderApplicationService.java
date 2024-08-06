package com.omegafrog.My.piano.app.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.external.tossPayment.Payment;
import com.omegafrog.My.piano.app.external.tossPayment.PaymentStatusChangedResult;
import com.omegafrog.My.piano.app.external.tossPayment.TossError;
import com.omegafrog.My.piano.app.external.tossPayment.TossPaymentInstance;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.exception.payment.CashOrderCalculateFailureException;
import com.omegafrog.My.piano.app.web.exception.payment.CashOrderConfirmFailedException;
import com.omegafrog.My.piano.app.web.exception.payment.TossAPIException;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrder;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrderRepository;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.order.CashOrderDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.dto.dateRange.CustomDateRange;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRangeFactory;
import com.omegafrog.My.piano.app.web.exception.payment.WrongOrderStateException;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashOrderApplicationService {

    private final CashOrderRepository cashOrderRepository;
    private final TossPaymentInstance tossPaymentInstance;
    @PersistenceUnit
    private EntityManagerFactory emf;
    private final DateRangeFactory dateRangeFactory;
    private final TossPaymentInstance paymentInstance;
    private final AuthenticationUtil authenticationUtil;

    @Transactional
    public CashOrderDto createCashOrder(String orderId, int amount, String name) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        CashOrder cashOrder = new CashOrder(orderId, name, amount, loggedInUser);
        CashOrder saved = cashOrderRepository.save(cashOrder);
        return saved.toDto();
    }

    /**
     * 토스 결제 승인 요청 API를 호출합니다
     * <ol>
     *     <li>
     *         cash order state를 IN_PROGRESSING으로 변경
     *     </li>
     *     <li>
     *         무결성 검사 성공 시 결제 요청에서 얻은 paymentKey를 데이터베이스에 저장
     *     </li>
     *     <li>
     *         결제 요청 시 저장한 cash order amount와 결제 승인 요청시 parameter로 받은 amount를 비교해 무결성 검사
     *      ({@link CashOrder#validate(int)})
     *     </li>
     *     <li>
     *         {@link TossPaymentInstance#requestCashOrder(CashOrder)}로 결제 승인 요청 전송
     *     </li>
     *     <li>
     *         결제한 유저의 cash 증가
     *     </li>
     *     <li>
     *         모든 작업 완료 이후 cashOrder의 state {@link OrderStatus#DONE}으로 변경
     *     </li>
     * </ol>
     *
     * @param paymentKey   : 토스 결제 요청 API에서 생성한 결제 객체를 구분하는 키
     * @param orderId      : 클라이언트에서 생성한 현금 주문을 식별하는 키
     * @param amount       : 총 결제 금액
     */
    public void requestCashOrder(String paymentKey, String orderId, int amount) throws JsonProcessingException {
        CashOrder byOrderId;
        User user;
        User loggedInUser = authenticationUtil.getLoggedInUser();

        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        // start transaction
        tx.begin();

        // cashOrder, user 조회
        byOrderId = entityManager.find(CashOrder.class, orderId);
        user = entityManager.find(User.class, loggedInUser.getId());

        if(user == null || byOrderId == null){
            entityManager.close();
            throw new EntityNotFoundException();
        }

        try {
            // 결제 승인 요청 시작. state 변경
            byOrderId.changeState(OrderStatus.IN_PROGRESS);

            // paymentKey column 추가
            byOrderId.update(paymentKey);
            // 결제 요청 전에 저장한 amount와 승인 요청시 전달받은 amount 비교
            byOrderId.validate(amount);

            // API 호출
            paymentInstance.requestCashOrder(byOrderId);
            // cash 업데이트
            user.chargeCash(amount);
            // order state 변경
            byOrderId.changeState(OrderStatus.DONE);
            tx.commit();

        } catch (EntityNotFoundException | CashOrderCalculateFailureException
                 | CashOrderConfirmFailedException | TossAPIException e
        ) {
            tx.rollback();
            tx.begin();
            byOrderId.changeState(OrderStatus.ABORTED);
            tx.commit();
            throw e;
        } finally {
            entityManager.merge(user);
            entityManager.merge(byOrderId);
            entityManager.close();
        }
    }
    @Transactional
    public Page<PaymentHistory> getPaymentHistory(LocalDate start, LocalDate end, Pageable pageable) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        // expired handling
        List<CashOrder> expired = cashOrderRepository.findExpired(loggedInUser.getId(), new CustomDateRange(LocalDate.now(), LocalDate.now()));
        for(CashOrder order : expired)
            order.changeState(OrderStatus.EXPIRED);

        Page<CashOrder> cashOrders;
        if (start != null && end != null)
            cashOrders = cashOrderRepository.
                    findByUserIdAndDate(loggedInUser.getId(), pageable, dateRangeFactory.calcDateRange(start, end));

        else cashOrders = cashOrderRepository.findByUserId(loggedInUser.getId(), pageable);
        return PageableExecutionUtils.getPage(
                cashOrders.getContent().stream().map(PaymentHistory::new).collect(Collectors.toList()),
                pageable,
                cashOrders::getTotalElements
        );
    }

    @Transactional
    public void expireCashOrder(PaymentStatusChangedResult tossWebHookResult) {
        Payment data = tossWebHookResult.getData();

        CashOrder byOrderId = cashOrderRepository.findByOrderId(data.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Cash order entity by orderId." + data.orderId()));
        if (data.status().equals("EXPIRED"))
            byOrderId.expire();
    }

    /**
     * 해당 스케줄링을 실행한 날짜 00:00~24:00 사이의 cash order를 검사해서 정리<br/>
     * state가 READY, IN_PROGRESS이고 유효기간이 만료된 cash order의 state를 EXPIRED로 변경
     *
     */
//    @Scheduled(cron ="0 0 0 * * ?")
    @Scheduled(cron="0 0/1 * * * ?")
    @Transactional
    @Async("ThreadPoolTaskExecutor")
    public void handleExpiredCashOrders(){
        LocalDate today = LocalDate.now();
        List<CashOrder> expiredCashOrders =
                cashOrderRepository.findExpired(new CustomDateRange(today, today));
        for(CashOrder order : expiredCashOrders) {
            log.debug("order:{}", order);
            order.changeState(OrderStatus.EXPIRED);
        }
    }

    public void cancelPayment(String orderId, String cancelReason) {
        CashOrder cashOrder = cashOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Cash order Entity. id:" + orderId));
        validate(cashOrder);
        tossPaymentInstance.cancelPayment(cashOrder.getPaymentKey(), cancelReason);
    }
    private void validate(CashOrder cashOrder ) {
        if (!cashOrder.getStatus().equals(OrderStatus.DONE))
            throw new WrongOrderStateException("계산이 완료된 주문이어야 합니다.");
        if (cashOrder.getPaymentKey() == null)
            throw new NullPointerException("PaymentKey가 없습니다.");
    }
}
