package com.omegafrog.My.piano.app.post.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private Author author;
    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String content;

    @NotNull
    private int likeCount;

    @OneToMany
    private List<Comment> replies = new ArrayList<>();

}
