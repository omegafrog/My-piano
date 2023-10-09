package com.omegafrog.My.piano.app.web.dto.user;

import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UserProfile {
    private Long id;
    private String name;
    private String profileSrc;
    private LoginMethod loginMethod;

    @Builder
    public UserProfile(Long id, String name, String profileSrc, LoginMethod loginMethod) {
        this.id = id;
        this.name = name;
        this.profileSrc = profileSrc;
        this.loginMethod = loginMethod;
    }
}
