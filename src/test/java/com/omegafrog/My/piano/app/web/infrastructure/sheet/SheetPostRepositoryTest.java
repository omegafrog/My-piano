package com.omegafrog.My.piano.app.web.infrastructure.sheet;

import com.omegafrog.My.piano.app.DataJpaTestConfig;
import com.omegafrog.My.piano.app.web.controller.DummyData;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@DataJpaTest
@Import(DataJpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SheetPostRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SheetPostRepository sheetPostRepository;

    private User a;

    @BeforeEach
    void setRepository() {
        a = userRepository.save(User.builder()
                .name("user")
                .phoneNum(new PhoneNum("010-1111-2222"))
                .email("artist1@gmail.com")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("profileSrc")
                .build());
    }

    @Test
    @DisplayName("악보 판매글을 추가하고 조회할 수 있어야 한다.")
    @Transactional
    void saveNFindTest() {
        //given

        //when
        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(a));
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        //then
        Assertions.assertThat(founded).isPresent().contains(saved);
    }

    @Test
    @DisplayName("악보 판매글을 수정할 수 있어야 한다.")
    void updateTest() {
        //given

        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(a));
        //when
        UpdateSheetPostDto updated = UpdateSheetPostDto.builder()
                .sheet(UpdateSheetDto.builder()
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .isSolo(false)
                        .difficulty(Difficulty.MEDIUM)
                        .lyrics(false)
                        .sheetUrl("changed")
                        .pageNum(5)
                        .instrument(Instrument.GUITAR_BASE)
                        .build())
                .title("changeTitle")
                .content("changedContent")
                .build();

        SheetPost updatedPost = saved.update(updated);
        Assertions.assertThat(updatedPost).isEqualTo(saved);
    }

    @Test
    @DisplayName("악보 판매글을 삭제할 수 있어야 한다.")
    void deleteTest() {
        //given

        SheetPost saved = sheetPostRepository.save(DummyData.sheetPost(a));
        //when
        sheetPostRepository.deleteById(saved.getId());
        Optional<SheetPost> founded = sheetPostRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }

}