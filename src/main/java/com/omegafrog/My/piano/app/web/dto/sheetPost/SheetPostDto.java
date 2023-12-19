package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class SheetPostDto extends SellableItemDto {
    private String content;
    private Double discountRate = 0d;
    private UserProfile artist;
    private SheetInfoDto sheet;
    private boolean likePost;

}
