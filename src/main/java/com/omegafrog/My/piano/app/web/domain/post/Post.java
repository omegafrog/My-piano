package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private User author;

    private LocalDateTime createdAt = LocalDateTime.now();

    @PositiveOrZero
    private int viewCount=0;

    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    @PositiveOrZero
    private int likeCount=0;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @NotNull
    private final List<Comment> comments = new ArrayList<>();

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
        comments.removeIf(comment -> comment.getId().equals(id));
    }

    @Builder
    public Post(User author,String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostDto toDto(){
        return PostDto.builder()
                .id(id)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .author(new UserProfile(author.getId(), author.getName(), author.getProfileSrc()))
                .likeCount(likeCount)
                .viewCount(viewCount)
                .comments(comments)
                .build();
    }

    public void addLikeCount(){
        likeCount++;
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

