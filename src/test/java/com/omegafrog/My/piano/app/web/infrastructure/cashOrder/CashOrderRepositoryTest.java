package com.omegafrog.My.piano.app.web.infrastructure.cashOrder;

import com.omegafrog.My.piano.app.DataJpaTestConfig;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrder;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrderRepository;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrderRepositoryImpl;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.dateRange.CustomDateRange;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@DataJpaTest
@Import(value = DataJpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CashOrderRepositoryTest {

    @Autowired
    private CashOrderRepository cashOrderRepository;
    @Autowired
    private CashOrderRepositoryImpl impl;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("해당 유저의 만료된 CashOrder를 조회할 수 있어야 한다.")
    void findExpiredTest() {
        // given
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CashOrder cashOrder = new CashOrder("cash-" + UUID.randomUUID().toString(), "5000원 테스트 결제",
                5000, userRepository.findById(1L).get());
        LocalDateTime testTime = LocalDateTime.of(2024, 01, 01, 0, 0);
        ReflectionTestUtils.setField(cashOrder, "createdAt", testTime);

        CashOrder saved = cashOrderRepository.save(cashOrder);

        //when
        List<CashOrder> expired = cashOrderRepository.findExpired(saved.getUserId(), new CustomDateRange(testTime.toLocalDate(), testTime.plusDays(1).toLocalDate()));

        //then
        System.out.println("expired = " + expired);
        Assertions.assertThat(expired).isNotEmpty();
        Assertions.assertThat(expired.get(0)).isEqualTo(saved);
    }

    @Test
    @DisplayName("유저ID로 cashorder를 조회할 수 있어야 한다")
    void findByUserIdTest() {
        //given
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));


        // create new CashOrder
        String orderId = "cash-" + UUID.randomUUID().toString();
        CashOrder cashOrder = new CashOrder(orderId,
                "test 결제", 1000, userRepository.findById(1L).get());
        ReflectionTestUtils.setField(cashOrder, "orderId", orderId);
        CashOrder saved = cashOrderRepository.save(cashOrder);

        //when
        Optional<CashOrder> founded = cashOrderRepository.findByOrderId(saved.getOrderId());

        //then
        Assertions.assertThat(founded).isNotEmpty().contains(saved);

    }

    @Test
    @DisplayName("유저 ID와 주어진 기간사이의 cashOrder를 조회할 수 있어야 한다.")
    void findByUserIdAndDateTest() {
        //given
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));


        // create new CashOrder
        String orderId = "cash-" + UUID.randomUUID().toString();
        CashOrder cashOrder = new CashOrder(orderId,
                "test 결제", 1000, userRepository.findById(1L).get());
        ReflectionTestUtils.setField(cashOrder, "orderId", orderId);
        ReflectionTestUtils.setField(cashOrder, "createdAt",
                LocalDateTime.of(2024, 01, 01, 0, 0));
        CashOrder saved = cashOrderRepository.save(cashOrder);

        System.out.println("saved.getCreatedAt() = " + saved.getCreatedAt());

        // create Pageable Implementation class that offset = 0 and pageSize = 10
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getOffset()).thenReturn(0L);
        Mockito.when(pageable.getPageSize()).thenReturn(10);

        //when
        CustomDateRange range = new CustomDateRange(saved.getCreatedAt().toLocalDate(), saved.getCreatedAt().plusDays(1).toLocalDate());
        Page<CashOrder> founded = cashOrderRepository.findByUserIdAndDate(saved.getUserId(), pageable, range);

        //then
        Assertions.assertThat(founded).isNotEmpty().contains(saved);
    }

    @Test
    @DisplayName("모든 유저의 만료된 cash order를 조회할 수 있어야 한다.")
    void findExpiredTest2(){
        //given
        // create 2 user entity
        User user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1L);
        User user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2L);

        // make userRepository.findById() return user1 and user2
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        CashOrder cashOrder1 =
                new CashOrder("cash-" + UUID.randomUUID(),
                        "test cash order 1",
                        1000,
                        userRepository.findById(1L).get());
        ReflectionTestUtils.setField(cashOrder1, "createdAt", LocalDateTime.of(2024, 01, 01, 0, 0));
        CashOrder cashOrder2 =
                new CashOrder("cash-" + UUID.randomUUID(),
                        "test cash order 2",
                        1000,
                        userRepository.findById(2L).get());
        ReflectionTestUtils.setField(cashOrder2, "createdAt", LocalDateTime.of(2024, 01, 01, 0, 0));

        cashOrderRepository.save(cashOrder1);
        cashOrderRepository.save(cashOrder2);

        //when
        CustomDateRange range = new CustomDateRange(
                LocalDateTime.of(2024, 01, 01, 0, 0).toLocalDate(),
                LocalDateTime.of(2024, 01, 01, 0, 0).plusDays(1).toLocalDate());
        List<CashOrder> expired = cashOrderRepository.findExpired(range);

        //then
        Assertions.assertThat(expired).isNotEmpty().hasSize(2);

    }

}

