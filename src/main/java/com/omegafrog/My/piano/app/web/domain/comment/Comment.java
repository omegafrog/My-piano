package com.omegafrog.My.piano.app.web.domain.comment;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt=LocalDateTime.now();

    private String content;

    private int likeCount;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "PARENT_ID")
    private List<Comment> replies = new CopyOnWriteArrayList<>();

    @ManyToOne
    private Article target;

    public void setTarget(Article article){
        target = article;
    }

    public void increaseLikeCount(){
        likeCount++;
    }
    public void decreaseLikeCount(){
        likeCount--;
    }

    @Builder
    public Comment(Long id, User author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.likeCount = 0;
    }

    public CommentDto toDto(){
        return CommentDto.builder()
                .id(id)
                .author(author.getUserInfo())
                .content(content)
                .createdAt(createdAt)
                .likeCount(likeCount)
                .replies(replies.stream().map(Comment::toDto).toList())
                .build();
    }

    public ReturnCommentDto toReturnCommentDto(){
        return ReturnCommentDto.builder()
                .id(id)
                .content(content)
                .targetId(target.getId())
                .likeCount(likeCount)
                .author(author.getUserInfo())
                .createdAt(createdAt)
                .build();
    }


}
