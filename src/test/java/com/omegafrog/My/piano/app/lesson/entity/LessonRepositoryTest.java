package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.enums.Category;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.enums.RefundPolicy;
import com.omegafrog.My.piano.app.sheet.entity.Sheet;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.Optional;

@DataJpaTest
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @AfterEach
    void deleteAll(){
        lessonRepository.deleteAll();
    }

    @Test
    @DisplayName("lesson을 추가하고 조회할 수 있어야 한다.")
    void addNFindTest(){
        Lesson lesson = Lesson.builder()
                .title("lesson1")
                .subTitle("good lesson 1")
                .price(12000)
                .lessonProvider(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
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
                .sheet(new Sheet())
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
                .lessonProvider(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
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
                .sheet(new Sheet())
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
                .sheet(new Sheet())
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
                .lessonProvider(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
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
                .sheet(new Sheet())
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