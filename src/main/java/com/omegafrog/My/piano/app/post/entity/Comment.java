package com.omegafrog.My.piano.app.post.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "AUTHOR_ID")
    private Author author;
    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String content;

    @NotNull
    private int likeCount;

    @OneToMany
    @JoinColumn(name = "PARENT_ID")
    private Queue<Comment> replies = new ConcurrentLinkedQueue<>();

    @Builder
    public Comment(Long id, Author author, LocalDateTime createdAt, String content, int likeCount) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.content = content;
        this.likeCount = likeCount;
    }


}
