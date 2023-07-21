package com.omegafrog.My.piano.app.web.infrastructure.lesson;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.sheetPost.JpaSheetPostRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.sheetPost.SimpleJpaSheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SimpleJpaUserRepository jpaUserRepository;

    private UserRepository userRepository;

    @Autowired
    private SimpleJpaSheetPostRepository jpaSheetPostRepository;


    private User author;

    private SheetPost savedSheetPost;
    @BeforeAll
    void settings(){
        userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
        SheetPostRepository sheetPostRepository = new JpaSheetPostRepositoryImpl(jpaSheetPostRepository);
        User build = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .isAuthorized(false)
                        .build())
                .cart(new Cart())
                .email("user1@gmail.com")
                .build();
        author = userRepository.save(build);
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
                        .user(author)
                        .build())
                .artist(author)
                .build();
         savedSheetPost = sheetPostRepository.save(sheetPost);
    }
    @AfterEach
    void deleteAll(){
        lessonRepository.deleteAll();
    }

    @AfterAll
    void clearRepository(){
        jpaSheetPostRepository.deleteAll();
    }
    @Test
    @DisplayName("lesson을 추가하고 조회할 수 있어야 한다.")
    void addNFindTest(){
        Lesson lesson = Lesson.builder()
                .title("lesson1")
                .subTitle("good lesson 1")
                .price(12000)
                .lessonProvider(author)
                .lessonInformation(LessonInformation.builder()
                        .lessonDescription("lessonDesc")
                        .artistDescription("artistDesc")
                        .instrument(Instrument.PIANO_KEY_61)
                        .artistDescription("HIHI")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .category(Category.ACCOMPANIMENT)
                        .build())
                .videoInformation(VideoInformation.builder()
                        .videoUrl("videoUrl1")
                        .runningTime(LocalTime.of(1, 10))
                        .build())
                .sheet(savedSheetPost.getSheet())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        Optional<Lesson> founded = lessonRepository.findById(saved.getId());
        Assertions.assertThat(saved).isEqualTo(founded.get());
    }

    @Test
    @DisplayName("레슨을 수정할 수 있어야 한다.")
    void updateTest(){
        //given
        Lesson lesson = Lesson.builder()
                .title("lesson1")
                .subTitle("good lesson 1")
                .price(12000)
                .lessonProvider(author)
                .lessonInformation(LessonInformation.builder()
                        .lessonDescription("lessonDesc")
                        .artistDescription("artistDesc")
                        .instrument(Instrument.PIANO_KEY_61)
                        .artistDescription("HIHI")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .category(Category.ACCOMPANIMENT)
                        .build())
                .videoInformation(VideoInformation.builder()
                        .videoUrl("videoUrl1")
                        .runningTime(LocalTime.of(1, 10))
                        .build())
                .sheet(savedSheetPost.getSheet())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        //when
        saved.update(UpdateLessonDto.builder()
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
                .sheet(saved.getSheet())
                .build());
        Lesson updated = lessonRepository.save(saved);
        //then
        Assertions.assertThat(updated).isEqualTo(saved);
    }

    @Test
    @DisplayName("레슨을 삭제할 수 있어야 한다.")
    void deleteTest(){
        //given
        Lesson lesson = Lesson.builder()
                .title("lesson1")
                .subTitle("good lesson 1")
                .price(12000)
                .lessonProvider(author)
                .lessonInformation(LessonInformation.builder()
                        .lessonDescription("lessonDesc")
                        .artistDescription("artistDesc")
                        .instrument(Instrument.PIANO_KEY_61)
                        .artistDescription("HIHI")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .category(Category.ACCOMPANIMENT)
                        .build())
                .videoInformation(VideoInformation.builder()
                        .videoUrl("videoUrl1")
                        .runningTime(LocalTime.of(1, 10))
                        .build())
                .sheet(savedSheetPost.getSheet())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        //when
        lessonRepository.deleteById(saved.getId());
        //then
        Long id = saved.getId();
        Optional<Lesson> founded = lessonRepository.findById(id);
        Assertions.assertThat(founded).isEmpty();
    }
}