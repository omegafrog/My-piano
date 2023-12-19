package com.omegafrog.My.piano.app.web.vo.user;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import lombok.Setter;

import javax.annotation.Nullable;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class PhoneNum  {

    @Pattern(regexp = "010-([0-9]{3,4})-([0-9]{4})")
    @Nullable
    private String phoneNum;
    private boolean isAuthorized=false;

    @Builder
    public PhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
