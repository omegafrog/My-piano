package com.omegafrog.My.piano.app.web.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRegisterDto {
    @NotNull
    private Long itemId;
    @NotNull
    private Long buyerId;
    private Long couponId;
}
