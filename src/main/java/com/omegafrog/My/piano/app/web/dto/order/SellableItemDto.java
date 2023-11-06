package com.omegafrog.My.piano.app.web.dto.order;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class SellableItemDto {
    protected Long id;
    protected String title;
    private int price;
    protected int viewCount;
    protected int likeCount;
    protected LocalDateTime createdAt;
}
