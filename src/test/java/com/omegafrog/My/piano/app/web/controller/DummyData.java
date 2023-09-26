package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;

import java.time.LocalTime;

public class DummyData {

    public static SheetPost sheetPost(User artist){
        return SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .filePath("path1")
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .user(artist)
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .title("SheetPostTItle1")
                .price(12000)
                .artist(artist)
                .content("hihi this is content")
                .build();
    }

    public static Lesson lesson(Sheet sheet, User artist){
        return Lesson.builder()
                .sheet(sheet)
                .title("lesson1")
                .price(2000)
                .lessonInformation(LessonInformation.builder()
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .lessonDescription("hoho")
                        .category(Category.ACCOMPANIMENT)
                        .artistDescription("god")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .build())
                .lessonProvider(artist)
                .subTitle("this is subtitle")
                .videoInformation(
                        VideoInformation.builder()
                                .videoUrl("url")
                                .runningTime(LocalTime.of(0, 20))
                                .build())
                .build();
    }

    public static Sheet sheet(User artist){
        return Sheet.builder()
                .title("title")
                .filePath("path1")
                .genres(Genres.builder().genre1(Genre.BGM).build())
                .user(artist)
                .difficulty(Difficulty.MEDIUM)
                .instrument(Instrument.GUITAR_ACOUSTIC)
                .isSolo(true)
                .lyrics(false)
                .pageNum(3)
                .build();
    }

}
