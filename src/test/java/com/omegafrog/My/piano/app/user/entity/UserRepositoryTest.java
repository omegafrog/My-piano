package com.omegafrog.My.piano.app.user.entity;

import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;


@DataJpaTest
class UserRepositoryTest {


    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저를 추가하고 조회할 수 있어야 한다.")
    void saveNFindTest() {
        User user = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .isAuthorized(false)
                        .build())
                .build();
        User saved = userRepository.save(user);
        Optional<User> founded = userRepository.findById(saved.getId());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }
}