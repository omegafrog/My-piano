package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class OrderTest {

    @Test
    void calculateTotalPrice() {
        //given
        User seller = new User();
        User buyer = new User();
        Lesson lesson = new Lesson("title",
                "subTitle",
                2000,
                new VideoInformation(),
                new User(),
                new Sheet(),
                new LessonInformation());
        Coupon coupon = new Coupon("name", "code", 0.3D,
                LocalDateTime.of(2024, 01, 01, 00, 00));
        Order order = new Order(seller, buyer, lesson, 0D, coupon);

        //when
        order.calculateTotalPrice();
        //then

        Assertions.assertThat(order.getTotalPrice()).isEqualTo((int)(2000 * 0.7));
    }
}