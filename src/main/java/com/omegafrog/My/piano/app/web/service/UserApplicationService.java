package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.ChangeUserDto;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import io.awspring.cloud.s3.ObjectMetadata;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserApplicationService {


    public static final String USER_ENTITY_NOT_FOUNT_ERROR_MSG = "Cannot find User entity : ";
    private final UserRepository userRepository;
    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private S3UploadFileExecutor s3UploadFileExecutor;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    public UserInfo getUserProfile(User loggedInUser){
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        return user.getUserInfo();
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
        return user.getUploadedPosts().stream().map(post -> post.toDto()).toList();
    }

    public List<ReturnCommentDto> getMyComments(User loggedInUser)
            throws PersistenceException {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getWroteComments().stream().map(Comment::toReturnCommentDto).toList();
    }

    public List<SheetPostDto> getPurchasedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER+ loggedInUser.getId()));
        return user.getPurchasedSheets().stream().map(item->item.getSheetPost().toDto()).toList();

    }

    public Page<SheetPostDto> uploadedSheetPost(User loggedInUser, Pageable pageable) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        List<SheetPost> uploadedSheetPosts = user.getUploadedSheetPosts();

        if(pageable.isUnpaged())
            return getAllUploadedSheetPost(pageable, uploadedSheetPosts);

        return getUploadedSheetPostByPageable(pageable, uploadedSheetPosts);
    }

    private static Page<SheetPostDto> getUploadedSheetPostByPageable(Pageable pageable, List<SheetPost> uploadedSheetPosts) {
        long fromIdx = pageable.getOffset();
        int toIdx = (pageable.getPageNumber() + 1) * pageable.getPageSize();
        if( toIdx >= uploadedSheetPosts.size())
            toIdx = uploadedSheetPosts.size();
        List<SheetPostDto> sheetPosts = uploadedSheetPosts.subList((int) fromIdx, toIdx)
                .stream().map(SheetPost::toDto).toList();

        return PageableExecutionUtils.getPage(sheetPosts, pageable, uploadedSheetPosts::size);
    }

    private static Page<SheetPostDto> getAllUploadedSheetPost(Pageable pageable, List<SheetPost> uploadedSheetPosts) {
        return PageableExecutionUtils.getPage(
                uploadedSheetPosts.stream().map(SheetPost::toDto).toList(),
                pageable,
                uploadedSheetPosts::size);
    }

    public List<SheetInfoDto> getScrappedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getScrappedSheetPosts().stream().map(sheetPost->sheetPost.getSheetPost().toInfoDto()).toList();
    }

    public List<UserInfo> getFollowingFollower(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));
        return user.getFollowed().stream().map(i->i.getFollower().getUserInfo()).toList();
    }

    public List<LessonDto> getPurchasedLessons(User loggedInUserProfile) {
        User userProfile = userRepository.findById(loggedInUserProfile.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUserProfile.getId()));
       return userProfile.getPurchasedLessons().stream().map(purchasedLesson->purchasedLesson.getLesson().toDto()).toList();
    }



    public List<SheetPost> getLikedSheets(User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity:" + loggedInUser.getId()));
        return user.getLikedSheetPosts().stream().map(UserLikedSheetPost::getSheetPost).toList();
    }

    public UserInfo changeUserInfo(String dto, User loggedInUser, MultipartFile profileImg) throws IOException {
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + loggedInUser.getId()));
        ChangeUserDto changeUserDto = mapperUtil.parseUpdateUserInfo(dto);

        // 비밀번호 수정
        // 비밀번호 수정을 위해 현재 비밀번호는 입력하였으나 바꿀 비밀번호를 입력하지 않은 경우
        if(!changeUserDto.getCurrentPassword().isBlank() && changeUserDto.getChangedPassword().isBlank())
            throw new IllegalArgumentException("변경할 비밀번호를 입력해야 합니다");
        // TODO : 변경할 비밀번호가 요구사항에 맞지 않는 경우 validate

        // 현재 비밀번호가 요청한 현재 비밀번호 값과 맞는지 확인
        if (passwordEncoder.matches(changeUserDto.getCurrentPassword(), user.getSecurityUser().getPassword())) {
            // 맞다면 비밀번호 변경
            user.getSecurityUser().changePassword(passwordEncoder.encode(changeUserDto.getChangedPassword()));
        }

        // 이미지 수정
        if(!profileImg.isEmpty()){
            List<String> nameList = Arrays.asList(profileImg.getOriginalFilename().split("\\."));
            if (nameList.size() <2) throw new IllegalArgumentException("Wrong image type.");
            String contentType;
            switch (nameList.get(nameList.size()-1)) {
                case "jpg", "jpeg":
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                    break;
                case "png":
                    contentType = MediaType.IMAGE_PNG_VALUE;
                    break;
                default:
                    throw new IllegalArgumentException("Wrong image type.");
            }
            if(!user.getProfileSrc().isBlank())
                s3UploadFileExecutor.removeProfileImg(user.getProfileSrc());

            s3UploadFileExecutor.uploadProfileImg(profileImg, profileImg.getOriginalFilename(),ObjectMetadata.builder()
                    .contentType(contentType).build());
        }

        user.update(changeUserDto);
        return user.getUserInfo();
    }
}
