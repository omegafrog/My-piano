package com.omegafrog.My.piano.app.web.infrastructure.sheet;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
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
    void setRepository(){
        sheetPostRepository = new JpaSheetPostRepositoryImpl(jpaRepository);
        userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
        User a = User.builder()
                .name("user")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .email("artist@gmail.com")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("profileSrc")
                .build();
        user = userRepository.save(a);

        User b = User.builder()
                .name("uploader")
                .profileSrc("none")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .email("artist@gmail.com")
                .cart(new Cart())
                .build();
        author = userRepository.save(b);
    }

    @AfterEach
    void clearRepository(){
        sheetPostRepository.deleteAll();
    }


    @Test
    @DisplayName("악보 판매글을 추가하고 조회할 수 있어야 한다.")
    @Transactional
    void saveNFindTest() {
        //given
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(user)
                        .build())
                .artist(author)
                .build();
        //when
        SheetPost saved = sheetPostRepository.save(sheetPost);
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        //then
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded).contains(saved);
    }

    @Test
    @DisplayName("악보 판매글을 수정할 수 있어야 한다.")
    void updateTest() {
        //given
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(user)
                        .build())
                .artist(author)
                .build();
        SheetPost saved = sheetPostRepository.save(sheetPost);
        //when
        UpdateSheetPostDto updated = UpdateSheetPostDto.builder()
                .sheetDto(UpdateSheetDto.builder()
                        .genre(Genre.CAROL)
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
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(user)
                        .build())
                .artist(author)
                .build();
        SheetPost saved = sheetPostRepository.save(sheetPost);
        //when
        sheetPostRepository.deleteById(saved.getId());
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }

    @AfterAll
    void clearAllReposiotry(){
        sheetPostRepository.deleteAll();
        userRepository.deleteAll();
    }

}