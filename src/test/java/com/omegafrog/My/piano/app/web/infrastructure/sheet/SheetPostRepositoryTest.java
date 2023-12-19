package com.omegafrog.My.piano.app.web.infrastructure.sheet;

import com.omegafrog.My.piano.app.web.controller.DummyData;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.sheetPost.JpaSheetPostRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.sheetPost.SimpleJpaSheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SheetPostRepositoryTest {

    @Autowired
    private SimpleJpaSheetPostRepository jpaRepository;
    @Autowired
    private SimpleJpaUserRepository jpaUserRepository;

    private UserRepository userRepository;
    private SheetPostRepository sheetPostRepository;

    private User user;
    private User author;

    @BeforeAll
    void setRepository() {
        sheetPostRepository = new JpaSheetPostRepositoryImpl(jpaRepository);
        userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
        User a = User.builder()
                .name("user")
                .phoneNum(new PhoneNum("010-1111-2222"))
                .email("artist1@gmail.com")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("profileSrc")
                .build();
        user = userRepository.save(a);

        User b = User.builder()
                .name("uploader")
                .profileSrc("none")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(new PhoneNum("010-1111-2222"))
                .email("artist2@gmail.com")
                .cart(new Cart())
                .build();
        author = userRepository.save(b);
    }

    @AfterEach
    void clearRepository() {
        sheetPostRepository.deleteAll();
    }


    @Test
    @DisplayName("악보 판매글을 추가하고 조회할 수 있어야 한다.")
    @Transactional
    void saveNFindTest() {
        //given

        //when
        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(user));
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        //then
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded).contains(saved);
    }

    @Test
    @DisplayName("악보 판매글을 수정할 수 있어야 한다.")
    void updateTest() {
        //given

        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(user));
        //when
        UpdateSheetPostDto updated = UpdateSheetPostDto.builder()
                .sheetDto(UpdateSheetDto.builder()
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .isSolo(false)
                        .difficulty(Difficulty.MEDIUM)
                        .lyrics(false)
                        .filePath("changed")
                        .pageNum(5)
                        .instrument(Instrument.GUITAR_BASE)
                        .build())
                .title("changeTitle")
                .content("changedContent")
                .build();

        SheetPost updatedPost = saved.update(updated);
        SheetPost updatedSheetPost = sheetPostRepository.save(updatedPost);
        Assertions.assertThat(updatedSheetPost).isEqualTo(saved);
    }

    @Test
    @DisplayName("악보 판매글을 삭제할 수 있어야 한다.")
    void deleteTest() {
        //given

        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(user));
        //when
        sheetPostRepository.deleteById(saved.getId());
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }

    @AfterAll
    void clearAllReposiotry() {
        sheetPostRepository.deleteAll();
        userRepository.deleteAll();
    }

}