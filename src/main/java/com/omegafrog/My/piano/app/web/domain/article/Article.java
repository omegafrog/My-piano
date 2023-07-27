package com.omegafrog.My.piano.app.web.domain.article;

import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public abstract class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotEmpty
    protected String title;
    @NotEmpty
    protected String content;
    @PositiveOrZero
    protected int viewCount=0;
    @PositiveOrZero
    protected int likeCount=0;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    protected User author;

    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    protected List<Comment> comments = new ArrayList<>();


    public void increaseLikedCount(){
        likeCount++;
    }

    public void decreaseLikedCount(){
        likeCount--;
    }


    /**
     * 댓글을 추가한다.
     * @param comment  추가할 comment entity
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    /**
     * id를 가진 댓글을 삭제한다.
     * @param id 삭제할 댓글의 id
     */
    public void deleteComment(Long id) {
        this.comments.removeIf(comment -> comment.getId().equals(id));
    }

    public List<Comment> getComments(){
        return this.comments;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Article article = (Article) o;

        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}