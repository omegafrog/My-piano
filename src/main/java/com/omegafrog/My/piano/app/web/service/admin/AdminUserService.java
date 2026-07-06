package com.omegafrog.My.piano.app.web.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.admin.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.admin.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonListDto;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.service.admin.option.PostStrategy;
import com.omegafrog.My.piano.app.web.service.admin.option.SheetPostStrategy;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Transactional
public class AdminUserService implements UserDetailsService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;
    private final SecurityUserRepository securityUserRepository;
    private final PostRepository postRepository;
    private final MapperUtil mapperUtil;
    private final SheetPostRepository sheetPostRepository;
    private final LessonRepository lessonRepository;
    private final AuthenticationUtil authenticationUtil;

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return securityUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationServiceException("Cannot find admin entity. username : " + username));
    }


    public void register(String username, String password, String name, String email, Role role) {
        securityUserRepository.save(SecurityUser.builder()
                .role(role)
                .password(passwordEncoder.encode(password))
                .username(username)
                .user(new User(name, email, new Cart(), LoginMethod.EMAIL, null, new PhoneNum(), 0))
                .build());
    }

    public AdminDto getAdminProfile() {
        User admin = authenticationUtil.getLoggedInUser();
        return new AdminDto(admin.getId(),
                admin.getName(), admin.getEmail(), admin.getSecurityUser().getRole());
    }

    public Page<ReturnSessionDto> getLoggedInUsers(Pageable pageable) {
        List<ReturnSessionDto> sessions = sessionRegistry.getAllPrincipals().stream()
                .filter(SecurityUser.class::isInstance)
                .map(SecurityUser.class::cast)
                .filter(user -> user.getRole() == Role.USER || user.getRole() == Role.CREATOR)
                .flatMap(user -> sessionRegistry.getAllSessions(user, false).stream()
                        .map(session -> new ReturnSessionDto(user, getLoggedInTime(session))))
                .toList();
        int start = Math.min((int) pageable.getOffset(), sessions.size());
        int end = Math.min(start + pageable.getPageSize(), sessions.size());
        return new PageImpl<>(sessions.subList(start, end), pageable, sessions.size());
    }

    private static LocalDateTime getLoggedInTime(SessionInformation session) {
        return LocalDateTime.ofInstant(session.getLastRequest().toInstant(), ZoneId.systemDefault());
    }

    public void disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + id));
        SecurityUser securityUser = user.getSecurityUser();
        if (securityUser.isLocked()) throw new IllegalStateException("이미 비활성화된 회원입니다.");
        securityUser.disable();
    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + id));
        SecurityUser securityUser = user.getSecurityUser();
        if (!securityUser.isLocked()) throw new IllegalStateException("이미 활성화된 회원입니다.");
        securityUser.enable();
    }

    public Long countLoggedInUsers() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(SecurityUser.class::isInstance)
                .map(SecurityUser.class::cast)
                .filter(user -> user.getRole() == Role.USER || user.getRole() == Role.CREATOR)
                .flatMap(user -> sessionRegistry.getAllSessions(user, false).stream())
                .count();
    }

    public void disconnectLoggedInUser(Long userId, Role role) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(SecurityUser.class::isInstance)
                .map(SecurityUser.class::cast)
                .filter(user -> user.getId().equals(userId) && user.getRole() == role)
                .flatMap(user -> sessionRegistry.getAllSessions(user, false).stream())
                .forEach(SessionInformation::expireNow);
    }

    public Page<UserDto> getAllUsers(Pageable pageable, SearchUserFilter filter) {
        Page<SecurityUser> all = securityUserRepository.findAllByFilter(pageable, filter);
        return all.map(UserDto::new);
    }

    public Long countAllUsers() {
        return securityUserRepository.count();
    }

    public void controlUser(Long id, ControlUserDto dto) {
        SecurityUser user = securityUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SecurityUser entity. id : " + id));
        if (dto.remove() != null)
            if (dto.remove() == true) securityUserRepository.deleteById(id);
        if (dto.locked() != null) {
            if (Boolean.TRUE.equals(dto.locked())) user.disable();
            else user.enable();
        }
        if (dto.role() != null) user.changeRole(dto.role());
    }

    public Page<PostListDto> getAllPosts(SearchPostFilter filter, Pageable pageable) {
        Page<Post> all = postRepository.findAll(filter, pageable);
        return all.map(PostListDto::new);
    }

    public void disablePost(Long id, Boolean disabled) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity. id : " + id));
        if (disabled) post.disable();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public void update(Long id, String options) throws JsonProcessingException {
        List<PostStrategy> strategies = mapperUtil.parseUpdatePostOption(options);
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity. id : " + id));
        for (PostStrategy s : strategies) {
            s.update(post);
        }
    }

    public void writeNotiPost(String body, SecurityUser admin) throws JsonProcessingException {
        Post post = mapperUtil.parsePostNotiJson(body, admin);
        postRepository.save(post);
    }

    public Page<SheetPostListDto> getAllSheetPosts(Pageable pageable, SearchSheetPostFilter searchUserFilter) {
        Page<SheetPostDto> all = sheetPostRepository.findAll(pageable, searchUserFilter);
        return PageableExecutionUtils.getPage(all.stream().map(item ->
                new SheetPostListDto(
                        item.getId(),
                        item.getTitle(),
                        item.getArtist().getName(),
                        item.getArtist().getProfileSrc(),
                        item.getSheet().getTitle(),
                        item.getSheet().getDifficulty(),
                        item.getSheet().getGenres(),
                        item.getSheet().getInstrument(),
                        item.getCreatedAt(),
                        item.getPrice())).toList(), pageable, () -> all.getTotalElements());
    }

    public void updateSheetPost(Long id, String options) throws JsonProcessingException {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet post Entity"));
        List<SheetPostStrategy> strategies = mapperUtil.parseUpdateSheetPostOption(options);
        strategies.forEach(s -> s.update(sheetPost));
    }

    public void deleteSheetPost(Long id) {
        sheetPostRepository.deleteById(id);
    }


    public Page<LessonListDto> getAllLessons(Pageable pageable, SearchLessonFilter searchLessonFilter) {
        Page<Lesson> all = lessonRepository.findAll(pageable, searchLessonFilter);
        List<LessonListDto> result = all.map(item -> new LessonListDto(item)).toList();
        return PageableExecutionUtils.getPage(result, pageable, all::getTotalElements);
    }
}
