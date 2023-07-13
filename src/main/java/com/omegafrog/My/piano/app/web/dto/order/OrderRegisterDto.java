package com.omegafrog.My.piano.app.web.dto.order;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class OrderRegisterDto {
    private Long itemId;
    private Long buyerId;
    private Long couponId;
}
