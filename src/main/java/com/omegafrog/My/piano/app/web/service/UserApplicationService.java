package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
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

    public UserProfile getUserProfile(User loggedInUser){
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        return user.getUserProfile();
    }


    public int chargeCash(int cash, User loggedInuser){
        User user = userRepository.findById(loggedInuser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInuser.getId()));
        return user.chargeCash(cash);
    }

    public List<PostDto> getMyCommunityPosts(User loggedInUser)
            throws PersistenceException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getUploadedPosts().stream().map(post -> ((Post)post).toDto()).toList();
    }

    public List<ReturnCommentDto> getMyComments(User loggedInUser)
            throws PersistenceException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getWroteComments().stream().map(Comment::toReturnCommentDto).toList();
    }

    public List<SheetInfoDto> getPurchasedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER+ loggedInUser.getId()));
        return user.getPurchasedSheets().stream().map(sheetPost->((SheetPost)sheetPost).toInfoDto()).toList();

    }

    public List<SheetInfoDto> uploadedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getUploadedSheetPosts().stream().map(sheetPost->((SheetPost)sheetPost).toInfoDto()).toList();
    }

    public List<SheetInfoDto> getScrappedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getScrappedSheetPosts().stream().map(sheetPost->((SheetPost)sheetPost).toInfoDto()).toList();
    }

    public List<com.omegafrog.My.piano.app.web.dto.user.UserProfile> getFollowingFollower(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getFollowed().stream().map(User::getUserProfile).toList();
    }

    public List<LessonDto> getPurchasedLessons(User loggedInUserProfile) {
        User userProfile = userRepository.findById(loggedInUserProfile.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUserProfile.getId()));
       return userProfile.getPurchasedLessons().stream().map(lesson->((Lesson)lesson).toDto()).toList();
    }

    public UserProfile updateUser(User loggedInUser, UpdateUserDto userDto) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.update(userDto).getUserProfile();

    }
}
