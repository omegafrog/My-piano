package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetPostDto extends SellableItemDto {
    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private int likeCount;
    private Double discountRate = 0d;
    private List<CommentDto> comments;
    private UserProfile author;
    private LocalDateTime createdAt;
    private SheetInfoDto sheet;
    private int price;
    private boolean likePost;

    @Builder
    public SheetPostDto(Long id, SheetInfoDto sheet, int price, Double discountRate, String title, String content,
                        int viewCount, int likeCount, UserProfile author, LocalDateTime createdAt, List<CommentDto> comments,
                        boolean likePost) {
        this.id = id;
        this.sheet = sheet;
        this.price = price;
        this.discountRate = discountRate;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.author = author;
        this.createdAt = createdAt;
        this.comments = comments;
        this.likePost = likePost;

    }
}
