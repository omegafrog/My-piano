package com.omegafrog.My.piano.app.dto;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.AlarmProperties;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterUserDto {
    private String name;
    private AlarmProperties alarmProperties;
    private LoginMethod loginMethod;
    private String profileSrc;
    private PhoneNum phoneNum;
    private Cart cart;
    @Builder
    public RegisterUserDto(String name, AlarmProperties alarmProperties, LoginMethod loginMethod, String profileSrc, PhoneNum phoneNum, Cart cart) {
        this.name = name;
        this.alarmProperties = alarmProperties;
        this.loginMethod = loginMethod;
        this.profileSrc = profileSrc;
        this.phoneNum = phoneNum;
        this.cart = cart;
    }

    public User toEntity(){
        return User.builder()
                .name(name)
                .loginMethod(loginMethod)
                .phoneNum(phoneNum)
                .cart(cart)
                .profileSrc(profileSrc)
                .build();
    }
}
