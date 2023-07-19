package com.omegafrog.My.piano.app.web.vo.user;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class PhoneNum {

    @Pattern(regexp = "010-([0-9]{3,4})-([0-9]{4})")
    private String phoneNum;
    private boolean isAuthorized;

    @Builder
    public PhoneNum(String phoneNum, boolean isAuthorized) {
        this.phoneNum = phoneNum;
        this.isAuthorized = false;
    }
}
