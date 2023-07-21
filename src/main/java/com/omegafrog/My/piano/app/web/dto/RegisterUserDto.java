package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterUserDto {
    private String username;
    private String password;
    private String name;
    private String email;
    private LoginMethod loginMethod;
    private String profileSrc;
    private PhoneNum phoneNum;

    @Builder
    public RegisterUserDto(String username, String password, String name, String email, LoginMethod loginMethod, String profileSrc, PhoneNum phoneNum) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.loginMethod = loginMethod;
        this.profileSrc = profileSrc;
        this.phoneNum = phoneNum;
    }



    public User toEntity(){
        return User.builder()
                .name(name)
                .loginMethod(loginMethod)
                .phoneNum(phoneNum)
                .cart(new Cart())
                .profileSrc(profileSrc)
                .build();
    }
}
