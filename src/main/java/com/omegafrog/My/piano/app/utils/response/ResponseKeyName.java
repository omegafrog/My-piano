package com.omegafrog.My.piano.app.utils.response;

public enum ResponseKeyName {
    UPLOADED_COMMUNITY_POSTS("posts", "올린 커뮤니티 포스트"),
    PURCHASED_LESSONS("lessons", "구매한 레슨 리스트"),
    UPLOADED_COMMENTS("comments", "작성한 댓글 리스트"),
    PURCHASED_SHEETS("sheets", "구매한 악보 리스트"),
    UPLOADED_SHEETS("sheets", "올린 악보 리스트"),
    SCRAPPED_SHEETS("sheets", "스크랩한 악보 리스트"),
    FOLLOWED_USERS("follower", "팔로우한 유저 리스트");

    public final String keyName;
    public final String description;

    ResponseKeyName(String keyName, String description) {
        this.keyName = keyName;
        this.description = description;
    }
}
