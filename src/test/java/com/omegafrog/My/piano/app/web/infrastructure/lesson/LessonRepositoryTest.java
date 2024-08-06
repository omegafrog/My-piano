package com.omegafrog.My.piano.app.web.infrastructure.lesson;

import com.omegafrog.My.piano.app.DataJpaTestConfig;
import com.omegafrog.My.piano.app.web.controller.DummyData;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;
import java.util.Optional;

@DataJpaTest
@Import(DataJpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SheetPostRepository sheetPostRepository;



    private User author;

    private SheetPost savedSheetPost;

    @BeforeEach
    void settings() {
        author = userRepository.save(User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .build())
                .cart(new Cart())
                .email("user1@gmail.com")
                .build());
        savedSheetPost = sheetPostRepository.save(DummyData.sheetPost(author));
    }

    @Test
    @DisplayName("lesson을 추가하고 조회할 수 있어야 한다.")
    void addNFindTest() {
        //given

        //when
        Lesson saved = lessonRepository.save(DummyData.lesson(savedSheetPost, author));

        //then
        Optional<Lesson> founded = lessonRepository.findById(saved.getId());
        Assertions.assertThat(founded).isNotEmpty().contains(saved);
    }

    @Test
    @DisplayName("레슨을 수정할 수 있어야 한다.")
    void updateTest() {
        //given

        Lesson saved = lessonRepository.save(DummyData.lesson(savedSheetPost, author));
        //when
        Lesson updated = saved.update(UpdateLessonDto.builder()
                .title("changedTitle")
                .subTitle("changedSubTitle")
                .price(11000)
                .lessonInformation(LessonInformation.builder()
                        .category(Category.SHEET_COMPLETE)
                        .lessonDescription("changedDescription")
                        .artistDescription("changedArtistDescription")
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .policy(RefundPolicy.REFUND_IN_15DAYS)
                        .build())
                .videoInformation(VideoInformation.builder()
                        .videoUrl("changeVideoUrl")
                        .runningTime(LocalTime.of(1, 12, 40))
                        .build()
                )
                .sheetId(saved.getSheetPost().getId())
                .build(), saved.getSheetPost());
        //then
        Assertions.assertThat(updated).isEqualTo(saved);
    }

    @Test
    @DisplayName("레슨을 삭제할 수 있어야 한다.")
    void deleteTest() {
        //given
        Lesson saved = lessonRepository.save(DummyData.lesson(savedSheetPost, author));
        //when
        lessonRepository.deleteById(saved.getId());
        //then
        Long id = saved.getId();
        Optional<Lesson> founded = lessonRepository.findById(id);
        Assertions.assertThat(founded).isEmpty();
    }

}