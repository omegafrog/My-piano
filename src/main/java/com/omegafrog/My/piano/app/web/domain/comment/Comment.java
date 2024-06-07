package com.omegafrog.My.piano.app.web.domain.comment;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent",orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name="PARENT_ID")
    private Comment parent;

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
    public Comment(Long id, User author, String content, Comment parent) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.likeCount = 0;
        this.parent = parent;
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


    public void addReply(Comment saved) {
        replies.add(saved);
    }
}
