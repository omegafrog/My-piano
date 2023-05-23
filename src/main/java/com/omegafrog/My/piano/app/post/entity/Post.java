package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.dto.PostDto;
import com.omegafrog.My.piano.app.dto.UpdatePostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity

@NoArgsConstructor
@Getter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User author;

    private LocalDateTime createdAt;

    private int viewCount;

    private String title;
    private String content;
    private int likeCount;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Comment> comments = new CopyOnWriteArrayList<>();

    public Post update(UpdatePostDto post){
        this.title = post.getTitle();
        this.content = post.getContent();
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
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
    }

    public PostDto toDto(){
        return PostDto.builder()
                .id(id)
                .author(author)
                .createdAt(createdAt)
                .viewCount(viewCount)
                .title(title)
                .content(content)
                .likeCount(likeCount)
                .comments(comments)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return id.equals(post.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

