package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@NoArgsConstructor
@Getter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    @NotNull
    private User author;

    private LocalDateTime createdAt = LocalDateTime.now();

    @PositiveOrZero
    private int viewCount;

    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    @PositiveOrZero
    private int likeCount;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @NotNull
    private final List<Comment> comments = new CopyOnWriteArrayList<>();

    public Post update(UpdatePostDto post){
        this.viewCount = post.getViewCount();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.likeCount = post.getLikeCount();
        return this;
    }

    public int addComment(Comment comment){
        this.comments.add(comment);
        return this.comments.size();
    }

    public void deleteComment(Long id){
        this.comments.forEach(comment -> {
            if (comment.getId().equals(id)) comments.remove(comment);
        });
    }

    @Builder
    public Post(User author,String title, String content) {
        this.author = author;
        this.viewCount = 0;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

