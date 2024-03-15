package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.admin.AdminRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
@Transactional
public class AdminUserService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Override
    public Admin loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationServiceException("Cannot find admin entity. username : " + username));
    }


    public void register(String username, String password, String email, String name, Role role){
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

    public List<UserInfo> getUsers(Pageable pageable){
        List<User> all = userRepository.findAll(pageable);
        return all.stream().map(User::getUserProfile).toList();
    }

    public void disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + id));
        SecurityUser securityUser = user.getSecurityUser();
        if(securityUser.isLocked()) throw new IllegalStateException("이미 비활성화된 회원입니다.");
        securityUser.disable();
    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. id : " + id));
        SecurityUser securityUser = user.getSecurityUser();
        if(!securityUser.isLocked()) throw new IllegalStateException("이미 활성화된 회원입니다.");
        securityUser.enable();
    }
}
