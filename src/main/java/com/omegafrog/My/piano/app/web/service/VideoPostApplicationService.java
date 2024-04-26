package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.CommentIndexOutOfBoundsException;
import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class VideoPostApplicationService implements CommentHandler {

    private final UserRepository userRepository;
    private final VideoPostRepository videoPostRepository;
    private final CommentRepository commentRepository;

    public VideoPostDto writePost(VideoPostRegisterDto post, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost build = VideoPost.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .videoUrl(post.getVideoUrl())
                .author(user)
                .build();
        return videoPostRepository.save(build).toDto();
    }

    public VideoPostDto findPostById(Long id) {
        return videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER)).toDto();
    }

    public VideoPostDto updatePost(Long id, UpdateVideoPostDto post, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));

        if(isAccessDeniedToVideoPost(user, videoPost))
            throw new AccessDeniedException("Cannot update other user's videoPost.");

        videoPost.update(post);
        return videoPost.toDto();
    }

    public void deletePost(Long id, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));
        if(isAccessDeniedToVideoPost(user, videoPost))
            throw new AccessDeniedException("Cannot delete other user's video post.");
        user.deleteUploadedVideoPost(videoPost);
        videoPostRepository.deleteById(id);
    }

    private static boolean isAccessDeniedToVideoPost(User user, VideoPost videoPost) {
        return !videoPost.getAuthor().equals(user);
    }

    @Override
    public List<CommentDto> addComment(Long articleId, RegisterCommentDto dto, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + articleId));
        Comment savedComment = commentRepository.save(Comment.builder()
                .content(dto.getContent())
                .author(user)
                .build());
        videoPost.addComment(savedComment);
        return videoPost.getComments().stream().map(Comment::toDto).toList();
    }

    @Override
    public List<CommentDto> deleteComment(Long id, Long commentId, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));
        videoPost.deleteComment(commentId, user);
        return videoPost.getComments().stream().map(Comment::toDto).toList();
    }

    @Override
    public void likeComment(Long id, Long commentId) {
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        videoPost.increaseCommentLikeCount(commentId);
    }
    @Override
    public void dislikeComment(Long id, Long commentId) {
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        videoPost.decreaseCommentLikeCount(commentId);
    }

    @Override
    public Page<CommentDto> getComments(Long articleId, Pageable pageable) {
        VideoPost videoPost = videoPostRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int toIdx = (int)offset+pageSize;
        if (toIdx > videoPost.getComments().size()) toIdx = videoPost.getComments().size();
        return PageableExecutionUtils.getPage(
                videoPost.getComments().subList((int) offset, toIdx).stream().map(Comment::toDto).toList(),
                pageable,
                () -> videoPost.getComments().size());
    }

    public void likePost(Long id, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));
        user.likeVideoPost(videoPost);
    }

    public void dislikePost(Long id, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        VideoPost videoPost = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST));
        user.dislikeVideoPost(videoPost);
    }

    public List<VideoPostDto> findAllVideoPosts(Pageable pageable) {
        return videoPostRepository.findAll(pageable).getContent().stream().map(VideoPost::toDto).toList();
    }

    @Override
    public CommentDto replyComment(Long id, Long commentId, String replyContent, User loggedInUser) {
        VideoPost post = videoPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        Comment comment = post.getComments().stream().filter(item -> item.getId().equals(commentId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Cannot find comment entity : " + commentId));
        commentRepository.findById(commentId)
                .orElseThrow(()->new EntityNotFoundException("Cannot find comment entity : " + commentId));
        Comment reply = Comment.builder().content(replyContent)
                .author(loggedInUser)
                .build();
        Comment saved = commentRepository.save(reply);
        comment.addReply(saved);
        return saved.toDto();
    }
}

