package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.admin.AdminRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
@Transactional
public class AdminUserService implements UserDetailsService {


    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUserRepository securityUserRepository;


    @Override
    public Admin loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationServiceException("Cannot find admin entity. username : " + username));
    }


    public void register(String username, String password, String email, String name, Role role) {
        adminRepository.save(Admin.builder()
                .email(email)
                .name(name)
                .role(role)
                .password(passwordEncoder.encode(password))
                .username(username)
                .build());
    }

    public AdminDto getAdminProfile(Admin loggedInAdmin) {
        Admin admin = adminRepository.findByUsername(loggedInAdmin.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("Cannot find Admin. id : " + loggedInAdmin.getId()));
        return admin.toDto();
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
}
