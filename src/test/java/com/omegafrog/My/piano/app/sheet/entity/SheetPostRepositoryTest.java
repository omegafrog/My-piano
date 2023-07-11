package com.omegafrog.My.piano.app.sheet.entity;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.enums.Difficulty;
import com.omegafrog.My.piano.app.enums.Genre;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
class SheetPostRepositoryTest {
    @Autowired
    private SheetPostRepository sheetPostRepository;

    @Test
    @DisplayName("악보 판매글을 추가하고 조회할 수 있어야 한다.")
    void saveNFindTest() {
        //given
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .price(12000)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(User.builder()
                                .name("user")
                                .phoneNum(PhoneNum.builder()
                                        .phoneNum("010-1111-2222")
                                        .isAuthorized(false)
                                        .build())
                                .loginMethod(LoginMethod.EMAIL)
                                .cart(new Cart())
                                .profileSrc("profileSrc")
                                .build())
                        .build())
                .artist(User.builder()
                        .name("uploader")
                        .profileSrc("none")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-2222")
                                .isAuthorized(true)
                                .build())
                        .cart(new Cart())
                        .build())
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
    void updateTest(){
        //given
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .price(12000)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(User.builder()
                                .name("user")
                                .phoneNum(PhoneNum.builder()
                                        .phoneNum("010-1111-2222")
                                        .isAuthorized(false)
                                        .build())
                                .loginMethod(LoginMethod.EMAIL)
                                .cart(new Cart())
                                .profileSrc("profileSrc")
                                .build())
                        .build())
                .artist(User.builder()
                        .name("uploader")
                        .profileSrc("none")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-2222")
                                .isAuthorized(true)
                                .build())
                        .cart(new Cart())
                        .build())
                .build();
        SheetPost saved = sheetPostRepository.save(sheetPost);
        //when
        UpdateSheetPostDto updated = UpdateSheetPostDto.builder()
                .sheet(Sheet.builder()
                        .genre(Genre.CAROL)
                        .isSolo(false)
                        .difficulty(Difficulty.MEDIUM)
                        .lyrics(false)
                        .price(12000)
                        .filePath("changed")
                        .user(User.builder()
                                .name("uploader")
                                .profileSrc("none")
                                .loginMethod(LoginMethod.EMAIL)
                                .phoneNum(PhoneNum.builder()
                                        .phoneNum("010-1111-2222")
                                        .isAuthorized(true)
                                        .build())
                                .cart(new Cart())
                                .build())
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
    void deleteTest(){
        //given
        SheetPost sheetPost = SheetPost.builder()
                .title("title")
                .content("content")
                .sheet(Sheet.builder()
                        .genre(Genre.BGM)
                        .price(12000)
                        .lyrics(false)
                        .isSolo(false)
                        .difficulty(Difficulty.EASY)
                        .filePath("path")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .pageNum(12)
                        .user(User.builder()
                                .name("user")
                                .phoneNum(PhoneNum.builder()
                                        .phoneNum("010-1111-2222")
                                        .isAuthorized(false)
                                        .build())
                                .loginMethod(LoginMethod.EMAIL)
                                .cart(new Cart())
                                .profileSrc("profileSrc")
                                .build())
                        .build())
                .artist(User.builder()
                        .name("uploader")
                        .profileSrc("none")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-2222")
                                .isAuthorized(true)
                                .build())
                        .cart(new Cart())
                        .build())
                .build();
        SheetPost saved = sheetPostRepository.save(sheetPost);
        //when
        sheetPostRepository.deleteById(saved.getId());
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }
}