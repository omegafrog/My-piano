package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.spi.CopyOnWrite;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User author;
    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String content;

    @NotNull
    private int likeCount;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "PARENT_ID")
    private List<Comment> replies = new CopyOnWriteArrayList<>();

    @Builder
    public Comment(Long id, User author,String content) {
        this.id = id;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.content = content;
        this.likeCount = 0;
    }


}
