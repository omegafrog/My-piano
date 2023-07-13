package com.omegafrog.My.piano.app.web.infrastructure.user;

import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
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
        //given
        User user = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .isAuthorized(false)
                        .build())
                .build();
        //when
        User saved = userRepository.save(user);
        Optional<User> founded = userRepository.findById(saved.getId());
        //then
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded).contains(saved);
    }

    @Test
    @DisplayName("유저를 수정할 수 있어야 한다.")
    void updateTest(){
        //given
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
        //when
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("updatedName")
                .profileSrc("updatedProfile")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1234-1234")
                        .isAuthorized(true)
                        .build())
                .build();

        User updated = saved.update(updateDto);
        User updatedUser = userRepository.save(updated);

        Assertions.assertThat(updatedUser).isEqualTo(saved);
    }

    @Test
    @DisplayName("유저를 삭제할 수 있어야 한다.")
    void deleteTest(){
        //given
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
        //when
        userRepository.deleteById(saved.getId());
        Optional<User> founded = userRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }
}