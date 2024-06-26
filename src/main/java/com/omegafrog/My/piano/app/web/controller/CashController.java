package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.external.tossPayment.PaymentStatusChangedResult;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.service.CashOrderApplicationService;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashOrderApplicationService cashOrderService;

    private final MapperUtil mapperUtil;

    @PostMapping("/webhook")
    public void expireCashOrder(@RequestBody String json) throws JsonProcessingException {
        PaymentStatusChangedResult tossWebHookResult =  (PaymentStatusChangedResult) mapperUtil.parseTossWebhook(json);
        cashOrderService.expireCashOrder(tossWebHookResult);
    }

    @GetMapping("/info")
    public JsonAPIResponse createCashOrder(String orderId, int amount, String name) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cashOrderService.createCashOrder(orderId, amount, name, loggedInUser);
        return new APISuccessResponse("Create cash order success.");
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CashOrderDto {
        public String paymentKey;
        public String orderId;
        public Integer amount;
    }

    @PostMapping("/request")
    public JsonAPIResponse requestCashOrder(@RequestBody CashOrderDto cashOrder) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        cashOrderService.requestCashOrder(cashOrder.paymentKey, cashOrder.orderId, cashOrder.amount, loggedInUser);
        return new APISuccessResponse("Request cash order success.");
    }

    @GetMapping
    public JsonAPIResponse<List<PaymentHistory>> getPaymentHistory(@Nullable @RequestParam LocalDate start,@Nullable @RequestParam LocalDate end, Pageable pageable) throws JsonProcessingException {
        List<PaymentHistory> paymentHistory =
                cashOrderService.getPaymentHistory( start, end, AuthenticationUtil.getLoggedInUser(), pageable);
        return new APISuccessResponse("get payment history success.", paymentHistory);
    }
}
