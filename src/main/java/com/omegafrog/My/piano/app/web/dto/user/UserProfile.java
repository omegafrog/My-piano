package com.omegafrog.My.piano.app.web.dto.user;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UserProfile {
    private Long id;
    private String name;
    private String profileSrc;

    public UserProfile(Long id, String name, String profileSrc) {
        this.id = id;
        this.name = name;
        this.profileSrc = profileSrc;
    }
}
