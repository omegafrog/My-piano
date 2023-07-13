package com.omegafrog.My.piano.app.web.vo.user;

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

    private String phoneNum;
    private boolean isAuthorized;

    @Builder
    public PhoneNum(String phoneNum, boolean isAuthorized) {
        this.phoneNum = phoneNum;
        this.isAuthorized = false;
    }
}
