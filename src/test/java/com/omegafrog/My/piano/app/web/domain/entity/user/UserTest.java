package com.omegafrog.My.piano.app.web.domain.entity.user;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.user.LoginMethod;
import com.omegafrog.My.piano.app.web.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("유저의 내용을 수정할 수 있다.")
    void updateUserTest(){
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        User updated = user.update(UpdateUserDto.builder()
                .name("hihi")
                .phoneNum(new PhoneNum("111-222-333", true))
                .profileSrc("changed")
                .build());
        Assertions.assertThat(updated).isEqualTo(user);
    }

}