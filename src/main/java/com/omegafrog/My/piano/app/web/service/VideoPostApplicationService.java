package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class VideoPostApplicationService {


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

}

