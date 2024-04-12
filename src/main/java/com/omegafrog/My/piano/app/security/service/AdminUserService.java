package com.omegafrog.My.piano.app.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
@Transactional
public class AdminUserService implements UserDetailsService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUserRepository securityUserRepository;
    private final PostRepository postRepository;
    private final MapperUtil mapperUtil;


    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return securityUserRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationServiceException("Cannot find admin entity. username : " + username));
    }


    public void register(String username, String password, String name, String email,  Role role) {
        securityUserRepository.save(SecurityUser.builder()
                .role(role)
                .password(passwordEncoder.encode(password))
                .username(username)
                .user(new User(name, email, new Cart(), LoginMethod.EMAIL, null, new PhoneNum(), 0))
                .build());
    }

    public SecurityUser getAdminProfile(SecurityUser loggedInAdmin) {
        SecurityUser admin = securityUserRepository.findByUsername(loggedInAdmin.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("Cannot find Admin. id : " + loggedInAdmin.getId()));
        return admin;
    }

    public List<ReturnSessionDto> getLoggedInUsers(Pageable pageable) {
        Role[] roles = {Role.USER, Role.CREATOR};
        List<RefreshToken> list = refreshTokenRepository.findAllByRole(roles, pageable);
        return list.stream().map(r -> {
            SecurityUser founded = securityUserRepository.findById(Long.valueOf(r.getId()))
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find SecurityUser entity. id : " + r.getId()));

            return new ReturnSessionDto(
                    founded.getUser().getId(), founded.getUser().getName(), founded.getUsername(),
                    founded.getUser().getLoginMethod(), r.getCreatedAt(), founded.getCreatedAt(),
                     founded.getRole());
        }).toList();
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
        return refreshTokenRepository.countByRole(Role.USER);
    }

    public void disconnectLoggedInUser(Long userId, Role role) {
        refreshTokenRepository.deleteByUserIdAndRole(userId, role);
    }

    public List<UserDto> getAllUsers(Pageable pageable, SearchUserFilter filter) {
        List<SecurityUser> all = securityUserRepository.findAll(pageable, filter);
        return all.stream().map(UserDto::new).toList();
    }

    public Long countAllUsers() {
        return securityUserRepository.count();
    }

    public void controlUser(Long id, ControlUserDto dto) {
        SecurityUser user = securityUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SecurityUser entity. id : " + id));
        if(dto.remove() != null)
            if(dto.remove() == true) securityUserRepository.deleteById(id);
        if (dto.locked() != null) {
            if (Boolean.TRUE.equals(dto.locked())) user.disable();
            else user.enable();
        }
        if(dto.role() != null) user.changeRole(dto.role());
    }

    public Page<PostListDto> getAllPosts(SearchPostFilter filter, Pageable pageable) {
        Page<Post> all = postRepository.findAll(filter, pageable);
        return all.map(PostListDto::new);
    }

    public void disablePost(Long id, Boolean disabled) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity. id : " + id));
        if(disabled) post.disable();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public void update(Long id, List<UpdatePostStrategy> strategies) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity. id : " + id));
        for(UpdatePostStrategy s : strategies){
            s.update(post);
        }
    }

    public void writeNotiPost(String body, SecurityUser admin) throws JsonProcessingException {
        Post post = mapperUtil.parsePostNotiJson(body, admin);
        postRepository.save(post);
    }
}
