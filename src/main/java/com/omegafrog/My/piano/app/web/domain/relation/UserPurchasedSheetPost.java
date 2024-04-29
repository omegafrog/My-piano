package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "user_purchased_sheet_post")
@NoArgsConstructor
public class UserPurchasedSheetPost implements PurchasedSheetPost{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "sheet_post_id")
    private SheetPost sheetPost;

    @Builder
    public UserPurchasedSheetPost(User user, SheetPost sheetPost) {
        this.user = user;
        this.sheetPost = sheetPost;
    }
}