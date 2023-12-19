package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.enums.PaymentType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CashOrderDto {
    private String orderKey;

    private String paymentKey;

    private PaymentType paymentType;

    private int amount;

    private LocalDateTime createdAt;

    private OrderStatus status;

}
