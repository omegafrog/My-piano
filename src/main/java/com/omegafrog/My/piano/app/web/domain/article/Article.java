package com.omegafrog.My.piano.app.web.domain.article;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.exception.article.CannotDecreaseLikeCountException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
public class Article implements Serializable {

    private static final long serialVersionUID = -4563624585622096923L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotEmpty
    protected String title;
    @NotEmpty
    protected String content;
    @PositiveOrZero
    @Setter
    protected int viewCount = 0;
    @PositiveOrZero
    @Setter
    protected int likeCount = 0;

    protected boolean disabled = false;

    @Setter
    @ManyToOne
    protected User author;

    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime createdAt = LocalDateTime.now();
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime modifiedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    protected List<Comment> comments = new ArrayList<>();

    public void increaseLikedCount() {
        likeCount++;
    }

    public void decreaseLikedCount() {
        if (likeCount == 0) throw new CannotDecreaseLikeCountException("Like count is already 0");
        likeCount--;
    }

    public void increaseViewCount() {
        viewCount++;
    }

    /**
     * 댓글을 추가한다.
     *
     * @param comment 추가할 comment entity
     */
    public void addComment(Comment comment) {
        comment.setTarget(this);
        this.comments.add(comment);
        if (!comment.getAuthor().getWroteComments().contains(comment))
            comment.getAuthor().addWroteComments(comment);
    }

    /**
     * id를 가진 댓글을 삭제한다.
     *
     * @param id 삭제할 댓글의 id
     */
    public void deleteComment(Long id, User loggedInUser) throws AccessDeniedException, EntityNotFoundException {
        boolean isCommentRemoved = this.comments.removeIf(comment -> {
            if (comment.getId().equals(id)) {
                if (comment.getAuthor().equals(loggedInUser)) {
                    comment.getAuthor().deleteWroteComments(comment, loggedInUser);
                    return true;
                } else throw new AccessDeniedException("Cannot delete other user's comment.");
            } else return false;
        });
        if (!isCommentRemoved)
            throw new EntityNotFoundException("Cannot find comment entity : " + id);
    }

    //    public void increaseCommentLikeCount(Long commentId){
//        Comment foundedComment = this.comments.stream().filter(comment -> comment.getId().equals(commentId))
//                .findFirst().orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT));
//        foundedComment.increaseLikeCount();
//    }
//    public void decreaseCommentLikeCount(Long commentId){ Comment foundedComment = this.comments.stream().filter(comment -> comment.getId().equals(commentId))
//            .findFirst().orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT));
//        foundedComment.decreaseLikeCount();
//    }
    public List<Comment> getComments(Pageable pageable) {
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int toIdx = (int) offset + pageSize;
        if (toIdx > comments.size()) toIdx = comments.size();
        return comments.subList((int) offset, toIdx);
    }

    public void disable() {
        this.disabled = true;
    }

    public void enable() {
        this.disabled = false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;
        Article article = (Article) o;
        return Objects.equals(getId(), article.getId());
    }
}
