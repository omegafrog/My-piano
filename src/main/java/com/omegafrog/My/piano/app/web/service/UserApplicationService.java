package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserApplicationService {

    public static final String USER_ENTITY_NOT_FOUNT_ERROR_MSG = "Cannot find User entity : ";
    private final UserRepository userRepository;

    public List<PostDto> getMyCommunityPosts(User loggedInUser)
            throws PersistenceException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        return user.getUploadedPosts().stream().map(Post::toDto).toList();
    }

    public List<ReturnCommentDto> getMyComments(User loggedInUser)
            throws PersistenceException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        return user.getWritedComments().stream().map(
                comment -> ReturnCommentDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .targetId(comment.getTarget().getId())
                        .likeCount(comment.getLikeCount())
                        .author(comment.getAuthor().getUserProfile())
                        .createdAt(comment.getCreatedAt())
                        .build()
        ).toList();
    }

    public List<SheetInfoDto> getPurchasedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG+ loggedInUser.getId()));
        return user.getPurchasedSheets().stream().map(
                sheetPost -> SheetInfoDto.builder()
                        .id(sheetPost.getId())
                        .title(sheetPost.getTitle())
                        .sheetUrl(sheetPost.getSheet().getFilePath())
                        .artist(sheetPost.getAuthor().getUserProfile())
                        .createdAt(sheetPost.getCreatedAt())
                        .build()
        ).toList();

    }

    public List<SheetInfoDto> uploadedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        return user.getUploadedSheets().stream().map(
                sheetPost -> SheetInfoDto.builder()
                        .id(sheetPost.getId())
                        .title(sheetPost.getTitle())
                        .sheetUrl(sheetPost.getSheet().getFilePath())
                        .artist(sheetPost.getAuthor().getUserProfile())
                        .createdAt(sheetPost.getCreatedAt())
                        .build()
        ).toList();
    }

    public List<SheetInfoDto> getScrappedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        return user.getScrappedSheets().stream().map(
                sheetPost -> SheetInfoDto.builder()
                        .id(sheetPost.getId())
                        .title(sheetPost.getTitle())
                        .sheetUrl(sheetPost.getSheet().getFilePath())
                        .artist(sheetPost.getAuthor().getUserProfile())
                        .createdAt(sheetPost.getCreatedAt())
                        .build()
        ).toList();
    }

    public List<com.omegafrog.My.piano.app.web.dto.user.UserProfile> getFollowingFollwer(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        return user.getFollowed().stream().map(
                follower -> com.omegafrog.My.piano.app.web.dto.user.UserProfile.builder()
                        .name(follower.getName())
                        .profileSrc(follower.getProfileSrc())
                        .id(follower.getId())
                        .build()
        ).toList();
    }

    public List<LessonDto> getPurchasedLessons(User loggedInUserProfile) {
        User userProfile = userRepository.findById(loggedInUserProfile.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUserProfile.getId()));
       return userProfile.getPurchasedLessons().stream().map(
                lesson -> LessonDto.builder()
                        .id(lesson.getId())
                        .lessonProvider(lesson.getAuthor().getUserProfile())
                        .lessonInformation(lesson.getLessonInformation())
                        .title(lesson.getTitle())
                        .viewCount(lesson.getViewCount())
                        .subTitle(lesson.getContent())
                        .sheet(lesson.getSheet().toSheetDto())
                        .build()
        ).toList();
    }

    public UserProfile updateUser(User loggedInUser, UpdateUserDto userDto) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(USER_ENTITY_NOT_FOUNT_ERROR_MSG + loggedInUser.getId()));
        User updated = user.update(userDto);
        User save = userRepository.save(updated);
        return UserProfile.builder()
                .name(save.getName())
                .profileSrc(save.getProfileSrc())
                .id(save.getId())
                .build();
    }
}
