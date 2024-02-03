package com.omegafrog.My.piano.app.web.domain.ranking;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;

public record PopularRankingItem(Long id, String title, Integer price, UserProfile userInfo, String sheetTitle) {
    public PopularRankingItem(SheetPost sheetPost){
        this(sheetPost.getId(), sheetPost.getTitle(), sheetPost.getPrice(), sheetPost.getAuthor().getUserProfile(), sheetPost.getSheet().getTitle());
    }
}
