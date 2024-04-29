package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "user_liked_video_post")
public class UserLikedVideoPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="video_post_id")
    private VideoPost videoPost;

    @Builder
    public UserLikedVideoPost(User user, VideoPost videoPost) {
        this.user = user;
        this.videoPost = videoPost;
    }
}