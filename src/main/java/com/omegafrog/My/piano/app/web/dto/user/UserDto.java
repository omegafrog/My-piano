package com.omegafrog.My.piano.app.web.dto.user;


import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.vo.user.AlarmProperties;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;

import java.time.LocalDateTime;

public record UserDto(Long id, String name, String email, String username, LoginMethod loginMethod, String profileSrc,
                      int point, int cash,
                      PhoneNum phoneNum, AlarmProperties alarmProperties, Role role, LocalDateTime createdAt,
                      LocalDateTime credentialChangedAt, boolean locked) {
    public UserDto(SecurityUser securityUser) {
        this(securityUser.getId(), securityUser.getUser().getName(), securityUser.getUser().getEmail(),
                securityUser.getUsername(), securityUser.getUser().getLoginMethod(),
                securityUser.getUser().getProfileSrc(), securityUser.getUser().getPoint(),
                securityUser.getUser().getCash(), securityUser.getUser().getPhoneNum(),
                securityUser.getUser().getAlarmProperties(), securityUser.getRole(), securityUser.getCreatedAt(),
                securityUser.getCredentialChangedAt(), securityUser.isLocked());
    }
}
