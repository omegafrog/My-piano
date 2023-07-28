package com.omegafrog.My.piano.app.web.dto.sheet;

import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetInfoDto extends SellableItemDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private UserProfile artist;
    private String sheetUrl;

    @Builder
    public SheetInfoDto(Long id, String title, LocalDateTime createdAt, UserProfile artist, String sheetUrl) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.artist = artist;
        this.sheetUrl = sheetUrl;
    }
}
