package com.omegafrog.My.piano.app.web.domain.cart;

import com.omegafrog.My.piano.app.utils.exception.cart.DuplicateItemOrderException;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class CartTest {
    @Test
    void addContent() {
        Cart cart = new Cart();
        Order order = Order.builder()
                .id(1L)
                .totalPrice(100)
                .build();

        cart.addContent(order);
        Assertions.assertThat(cart.getContents()).contains(order);
    }

    @Test
    void cannotAddCollapsedOrderTest(){
        Cart cart = new Cart();
        SheetPost sheetPost =SheetPost.builder()
                .title("title")
                .content("content")
                .price(100)
                .build();

        ReflectionTestUtils.setField(sheetPost, "id", 1L);

        Order order = Order.builder()
                .id(1L)
                .totalPrice(100)
                .item(sheetPost)
                .build();

        Order sameItemOrder = Order.builder()
                .id(2L)
                .totalPrice(100)
                .item(sheetPost)
                .build();

        cart.addContent(order);
        Assertions.assertThatThrownBy(() -> cart.addContent(sameItemOrder))
                .isInstanceOf(DuplicateItemOrderException.class);
        Assertions.assertThat(cart.getContents()).doesNotContain(sameItemOrder);
    }
    @Test
    void deleteContent() {
        Cart cart = new Cart();
        cart.addContent(Order.builder().id(1L).totalPrice(100).build());

        cart.deleteContent(1L);
        Assertions.assertThat(cart.getContents()).isEmpty();
    }

    @Test
    void deleteNonExistingOrderTest(){
        Cart cart = new Cart();
        cart.addContent(Order.builder().id(1L).totalPrice(100).build());

        Assertions.assertThatThrownBy(() -> cart.deleteContent(2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void payAllContents() {
        User user = makeUser();
        Integer initialCash = user.getCash();

        User artist = makeArtist();

        SheetPost item1 =new SheetPost( "title1", "content1", artist,new Sheet(), 100);
        Lesson item2 = new Lesson("title2", "content2", 200,
                VideoInformation.builder().build(),
                artist,
                item1,
                LessonInformation.builder().build());
        Order order1 = new Order(artist, user, item1, 0.0, null);
        Order order2 = new Order(artist, user, item2, 0.0, null);

        user.getCart().addContent(order1);
        user.getCart().addContent(order2);

        user.getCart().payAllContents();
        Assertions.assertThat(user.getCash())
                .isEqualTo(initialCash - order1.getTotalPrice() - order2.getTotalPrice());
    }

    private static User makeArtist() {
        User artist = User.builder()
                .cash(0)
                .build();
        return artist;
    }

    private static User makeUser() {
        User user = User.builder()
                .cart(new Cart())
                .cash(1000).build();
        return user;
    }

    @Test
    void itemIsInCart() {
        Cart cart = new Cart();

        SheetPost sheetPost = makeSheetPost();

        Order order = Order.
                builder()
                .id(1L)
                .totalPrice(100)
                .item(sheetPost)
                .build();

        cart.addContent(order);
        Assertions.assertThat(cart.itemIsInCart(sheetPost.getId())).isTrue();
    }

    private static SheetPost makeSheetPost() {
        SheetPost sheetPost = new SheetPost("title1", "content1", new User(), new Sheet(), 100);
        ReflectionTestUtils.setField(sheetPost, "id", 1L);
        return sheetPost;
    }
}
