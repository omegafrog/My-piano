package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class SheetPostDto extends SellableItemDto {
    private String title;
    private String content;
    private Double discountRate = 0d;
    private ArtistInfo artist;
    private SheetInfoDto sheet;
    private boolean likePost;
}
