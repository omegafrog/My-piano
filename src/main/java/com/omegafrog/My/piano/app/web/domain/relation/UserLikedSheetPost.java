package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_liked_sheet_post")
@NoArgsConstructor
public class UserLikedSheetPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    @ManyToOne
    private SheetPost sheetPost;

    @Builder
    public UserLikedSheetPost(User user, SheetPost sheetPost) {
        this.user = user;
        this.sheetPost = sheetPost;
    }
}