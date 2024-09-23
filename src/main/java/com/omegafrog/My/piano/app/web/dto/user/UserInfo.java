package com.omegafrog.My.piano.app.web.dto.user;

import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String profileSrc;
    private LoginMethod loginMethod;
    private Role role;
    private int cash;
    private boolean enabled;
}
