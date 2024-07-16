package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Entity
@Table(name = "user_liked_video_post")
@NoArgsConstructor
public class UserLikedVideoPost implements Serializable {
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