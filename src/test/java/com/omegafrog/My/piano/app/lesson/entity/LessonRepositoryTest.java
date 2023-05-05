package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.lesson.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.lesson.entity.enums.Category;
import com.omegafrog.My.piano.app.lesson.entity.enums.Instrument;
import com.omegafrog.My.piano.app.lesson.entity.enums.RefundPolicy;
import com.omegafrog.My.piano.app.sheet.Sheet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityNotFoundException;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
                .lessonProvider(LessonProvider.builder()
                        .name("artist1")
                        .profileSrc("none").build())
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
                .lessonProvider(LessonProvider.builder()
                        .name("artist1")
                        .profileSrc("none").build())
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
                .lessonProvider(LessonProvider.builder()
                        .name("artist1")
                        .profileSrc("none").build())
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