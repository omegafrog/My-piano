package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class SheetPostDto extends SellableItemDto {
    private String content;
    private Double discountRate = 0d;
    private UserProfile author;
    private SheetInfoDto sheet;
    private boolean likePost;

}
