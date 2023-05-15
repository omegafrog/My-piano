package com.omegafrog.My.piano.app.user.vo;

import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
public class PhoneNum {

    private String phoneNum;
    private boolean isAuthorized;

    @Builder
    public PhoneNum(String phoneNum, boolean isAuthorized) {
        this.phoneNum = phoneNum;
        this.isAuthorized = false;
    }
}
