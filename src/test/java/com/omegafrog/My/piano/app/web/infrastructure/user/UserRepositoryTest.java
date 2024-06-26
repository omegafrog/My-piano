package com.omegafrog.My.piano.app.web.infrastructure.user;

import com.omegafrog.My.piano.app.DataJpaUnitConfig;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.dto.ChangeUserDto;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;


@DataJpaTest
@Import(value = DataJpaUnitConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {


    @Autowired
    private SimpleJpaUserRepository jpaUserRepository;

    private UserRepository userRepository;

    private User user1;

    @BeforeAll
    void settings() {
        userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
    }

    @AfterEach
    void clearRepository() {
        userRepository.deleteAll();
    }


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
                        .build())
                .email("user1@gmail.com")
                .cart(new Cart())
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
    void updateTest() {
        //given
        User user = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .build())
                .email("user1@gmail.com")
                .cart(new Cart())
                .build();
        User saved = userRepository.save(user);
        //when
        ChangeUserDto updateDto = ChangeUserDto.builder()
                .name("updatedName")
                .profileSrc("updatedProfile")
                .phoneNum("010-1234-1234")
                .build();

        User updated = saved.update(updateDto);
        User updatedUser = userRepository.save(updated);

        Assertions.assertThat(updatedUser).isEqualTo(saved);
    }

    @Test
    @DisplayName("유저를 삭제할 수 있어야 한다.")
    void deleteTest() {
        //given
        User user = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .build())
                .email("user1@gmail.com")
                .cart(new Cart())
                .build();
        User saved = userRepository.save(user);
        //when
        userRepository.deleteById(saved.getId());
        Optional<User> founded = userRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }
}