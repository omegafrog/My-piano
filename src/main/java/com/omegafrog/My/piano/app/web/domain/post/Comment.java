package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
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
    private LocalDateTime createdAt=LocalDateTime.now();

    private String content;

    private int likeCount;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "PARENT_ID")
    private List<Comment> replies = new CopyOnWriteArrayList<>();

    @Builder
    public Comment(Long id, User author,String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.likeCount = 0;
    }


    public CommentDto toDto(){
        return CommentDto.builder()
                .id(id)
                .content(content)
                .createdAt(createdAt)
                .replies(replies)
                .author(new UserProfile(author.getId(),author.getName(),author.getProfileSrc()))
                .likeCount(likeCount)
                .build();
    }


}
