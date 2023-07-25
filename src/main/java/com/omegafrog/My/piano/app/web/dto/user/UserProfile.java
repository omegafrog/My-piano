package com.omegafrog.My.piano.app.web.dto.user;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UserProfile {
    private Long id;
    private String name;
    private String profileSrc;

    @Builder
    public UserProfile(Long id, String name, String profileSrc) {
        this.id = id;
        this.name = name;
        this.profileSrc = profileSrc;
    }
}
