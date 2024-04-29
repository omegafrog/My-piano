package com.omegafrog.My.piano.app.web.domain.relation;

import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "followed_user")
public class FollowedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "followee_id")
    private User me;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;
}