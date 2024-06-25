package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.comment.CommentTargetType;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentApplicationService {

    private final CommentRepository commentRepository;
    private final SheetPostRepository sheetPostRepository;
    private final LessonRepository lessonRepository;
    private final PostRepository postRepository;
    private final VideoPostRepository videoPostRepository;
    private final UserRepository userRepository;
    private final AuthenticationUtil authenticationUtil;

    /**
     * 글에 달린 모든 댓글을 조회한다
     * @param articleId 글 id
     * @param pageable {@link Pageable} 객체
     * @return {@link Page}<{@link CommentDto}> 조회한 댓글이 담긴 Page 객체
     */
    public Page<CommentDto> getComments(Long articleId, Pageable pageable) {
        Page<Comment> allByTargetId = commentRepository.findAllByTargetId(articleId, pageable);

        return PageableExecutionUtils.getPage(
                allByTargetId.get().map(Comment::toDto).toList(),
                pageable,
                allByTargetId::getTotalElements);
    }

    /**
     * 글에 댓글을 추가한다.
     * @param type {@link CommentTargetType} 댓글을 작성할 글의 종류
     * @param targetId 댓글을 작성할 글의 id
     * @param dto {@link RegisterCommentDto}댓글의 내용
     * @param loggedInUser {@link User}로그인한 유저
     * @return {@link List}&lt;{@link CommentDto}&gt;댓글이 추가된 모든 댓글 리스트
     */
    public List<CommentDto> addComment(CommentTargetType type, Long targetId, RegisterCommentDto dto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        Article target = getTarget(type, targetId);

        Comment savedComment = commentRepository.save(
                Comment.builder()
                        .content(dto.getContent())
                        .author(user)
                        .build());
        target.addComment(savedComment);
        return target.getComments().stream().map(Comment::toDto).toList();
    }

    public void deleteComment(CommentTargetType type, Long targetId,  Long commentId ) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Article target = getTarget(type, targetId);
        boolean removed = target.getComments()
                .removeIf(comment -> comment.getId().equals(commentId) && comment.getAuthor().equals(loggedInUser));
        if(!removed) throw new EntityNotFoundException("Cannot delete Comment entity. id : " + commentId);
    }

    public CommentDto replyComment(Long commentId, String replyContent) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity : " + loggedInUser.getId()));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find comment entity : " + commentId));
        Comment reply = Comment.builder().content(replyContent)
                .author(user)
                .parent(comment)
                .build();
        Comment saved = commentRepository.save(reply);
        comment.addReply(saved);
        return saved.toDto();
    }
    private Article getTarget(CommentTargetType type, Long targetId) {
        return
            switch (type) {
                case POST -> postRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity. id:" + targetId));
                case SHEET_POST -> sheetPostRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity. id:" + targetId));
                case VIDEO_POST -> videoPostRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find VideoPost entity. id:" + targetId));
                case LESSON -> lessonRepository.findById(targetId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find Lesson entity. id:" + targetId));
            };
    }
}
