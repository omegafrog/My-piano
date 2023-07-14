package com.omegafrog.My.piano.app.web.dto.order;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRegisterDto {
    private Long itemId;
    private Long buyerId;
    private Long couponId;
}
