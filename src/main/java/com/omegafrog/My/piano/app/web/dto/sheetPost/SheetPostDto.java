package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetPostDto {
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

    @Builder
    public SheetPostDto(Long id, SheetInfoDto sheet, int price, Double discountRate, String title, String content, int viewCount, int likeCount, UserProfile author, LocalDateTime createdAt, List<CommentDto> comments) {
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
    }
}
