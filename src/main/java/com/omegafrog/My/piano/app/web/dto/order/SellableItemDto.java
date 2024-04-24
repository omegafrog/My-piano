package com.omegafrog.My.piano.app.web.dto.order;

import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class SellableItemDto {
    protected Long id;
    protected String title;
    protected int price;
    protected int viewCount;
    protected int likeCount;
    protected LocalDateTime createdAt;
    protected List<CommentDto> comments;
    protected Boolean disabled;

}
