package com.omegafrog.My.piano.app.web.dto.user;

import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import lombok.Builder;
import lombok.Data;

@Data
public class UpdateUserDto {
    private String name;
    private String profileSrc;
    private PhoneNum phoneNum;

    @Builder
    public UpdateUserDto(String name, String profileSrc, PhoneNum phoneNum) {
        this.name = name;
        this.profileSrc = profileSrc;
        this.phoneNum = phoneNum;
    }
}
