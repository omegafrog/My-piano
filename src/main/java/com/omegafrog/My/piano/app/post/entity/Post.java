package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private Author author;

    private LocalDateTime createdAt;

    private int viewCount;

    private String title;
    private String content;
    private int likeCount;

    public Post update(UpdatePostDto post){
        this.viewCount = post.getViewCount();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.likeCount = post.getLikeCount();
        return this;
    }

    @Builder
    public Post(Author author, LocalDateTime createdAt, int viewCount, String title, String content, int likeCount) {
        this.author = author;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
    }

    @OneToMany
    private List<Comment> comments = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (viewCount != post.viewCount) return false;
        if (likeCount != post.likeCount) return false;
        if (!id.equals(post.id)) return false;
        if (!author.equals(post.author)) return false;
        if (!createdAt.equals(post.createdAt)) return false;
        if (!title.equals(post.title)) return false;
        return content.equals(post.content);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + createdAt.hashCode();
        result = 31 * result + viewCount;
        result = 31 * result + title.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + likeCount;
        return result;
    }
}

