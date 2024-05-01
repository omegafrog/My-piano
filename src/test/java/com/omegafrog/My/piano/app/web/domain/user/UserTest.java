package com.omegafrog.My.piano.app.web.domain.user;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.ChangeUserDto;
import com.omegafrog.My.piano.app.web.enums.PostType;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserTest {

    @Test
    @DisplayName("유저의 내용을 수정할 수 있다.")
    void updateUserTest(){
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        User updated = user.update(ChangeUserDto.builder()
                .name("hihi")
                .phoneNum("111-222-333")
                .profileSrc("changed")
                .build());
        Assertions.assertThat(updated).isEqualTo(user);
    }

    @Test
    @DisplayName("현금을 충전할 수 있어야 한다.")
    void chargeCash() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        user.chargeCash(1000);
        Assertions.assertThat(user.getCash()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Post를 작성하면 UploadedPost List에 post entity가 추가되어야 한다.")
    void addUploadedPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = new Post(user, "title", "content", PostType.COMMON);
        user.addUploadedPost(post);
        Assertions.assertThat(user.getUploadedPosts()).hasSize(1).contains(post);
    }

    @Test
    @DisplayName("video post를 업로드 하면 List에 entity가 추가되어야 한다")
    void addUploadedVideoPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        VideoPost post = new VideoPost(user, "title", "content", "testVideoUrl");
        user.addUploadedVideoPost(post);
        Assertions.assertThat(user.getUploadedVideoPosts()).hasSize(1).contains(post);
    }
    @Test
    @DisplayName("lesson을 scrap할 수 있어야 한다.")
    @Order(1)
    void scrapLesson() {
        //given
        User author = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);
        Lesson lesson = new Lesson();
        ReflectionTestUtils.setField(lesson, "id", 1L);
        author.getUploadedLessons().add(lesson);

        User scrappedUser = User.builder().build();

        scrappedUser.scrapLesson(lesson);
        Assertions.assertThat(scrappedUser.getScrappedLesson()).contains(lesson);
    }
    @Test
    @DisplayName("유저가 해당 Lesson entity가 스크랩한 Lesson entity인지 알 수 있어야 한다.")
    @Order(2)
    void isScrappedLesson() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Lesson lesson = new Lesson();
        ReflectionTestUtils.setField(lesson, "id", 1L);
        user.getUploadedLessons().add(lesson);

        user.scrapLesson(lesson);

        Assertions.assertThat(user.isScrappedLesson(lesson)).isTrue();
        Assertions.assertThat(user.getScrappedLesson()).contains(lesson);
    }

    @Test
    @DisplayName("유저 엔티티는 스크랩한 sheet post를 List에 추가해야 한다")
    void scrapSheetPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        SheetPost post = new SheetPost();
        user.scrapSheetPost(post);
        Assertions.assertThat(user.getScrappedSheetPosts()).contains(post);
    }

    @Test
    @DisplayName("유저 엔티티는 좋아요를 누른 sheet post를 저장해야 한다.")
    void likeSheetPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        SheetPost post = new SheetPost();
        user.likeSheetPost(post);
        Assertions.assertThat(user.getLikedSheetPosts()).contains(post);
    }

    @Test
    @DisplayName("유저 엔티티는 좋아요를 누른 sheet post를 취소할 수 있어야 한다.")
    void dislikeSheetPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        SheetPost post = new SheetPost();
        user.likeSheetPost(post);
        user.dislikeSheetPost(post);
        Assertions.assertThat(user.getLikedSheetPosts()).doesNotContain(post);
    }

    @Test
    @DisplayName("유저 엔티티는 업로드한 post를 삭제할 수 있어야 한다.")
    void deleteUploadedPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = new Post();
        user.addUploadedPost(post);
        user.deleteUploadedPost(post);
        Assertions.assertThat(user.getUploadedPosts()).doesNotContain(post);
    }

    @Test
    @DisplayName("유저는 좋아요를 누른 post를 list에 추가해야 한다 ")
    void addLikePost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = new Post();
        user.likePost(post);
        Assertions.assertThat(user.getLikedPosts()).contains(post);
    }

    @Test
    @DisplayName("좋아요를 누른 video post를 list에 추가해야 한다")
    void likeVideoPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        VideoPost post = new VideoPost();
        user.likeVideoPost(post);
        Assertions.assertThat(user.getLikedVideoPosts()).contains(post);
    }

    @Test
    @DisplayName("좋아요를 누른 video post를 취소할 수 있어야 한다.")
    void dislikeVideoPost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        VideoPost post = new VideoPost();
        user.likeVideoPost(post);
        user.dislikeVideoPost(post);
        Assertions.assertThat(user.getLikedVideoPosts()).doesNotContain(post);
    }

    @Test
    @DisplayName("작성한 comments는 commentList에 저장되어야 한다.")
    void addWroteComments() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Comment comment = new Comment();
        user.addWroteComments(comment);

        Assertions.assertThat(user.getWroteComments()).contains(comment);
    }

    @Test
    @DisplayName("작성한 comment를 삭제할 수 있어야 한다.")
    void deleteWroteComments() {

        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Comment comment = new Comment(1L, user,"content");
        user.addWroteComments(comment);
        user.deleteWroteComments(comment, user);
        Assertions.assertThat(user.getWroteComments()).doesNotContain(comment);
    }

    @Test
    @DisplayName("유저 엔티티를 업데이트 할 수 있어야 한다.")
    void update() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ChangeUserDto changeUserDto = ChangeUserDto.builder()
                .name("newName")
                .profileSrc("newProfileSrc")
                .build();
        user.update(changeUserDto);
        Assertions.assertThat(user.getName()).isEqualTo("newName");
        Assertions.assertThat(user.getProfileSrc()).isEqualTo("newProfileSrc");
    }

    @Test
    @DisplayName("좋아요를 누른 post를 취소할 수 있다")
    void dislikePost() {
        User user = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Post post = new Post();
        user.likePost(post);
        user.dislikePost(post);
        Assertions.assertThat(user.getLikedPosts()).doesNotContain(post);
    }

    @Test
    @DisplayName("유저는 물건을 구매할 수 있어야 한다.")
    void pay() {
        User buyer = User.builder()
                .name("hi")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("hi")
                .phoneNum(new PhoneNum())
                .build();
        User seller = new User();
        Lesson item = new Lesson();
        ReflectionTestUtils.setField(buyer, "cash", 1500);
        ReflectionTestUtils.setField(buyer, "id", 1L);
        ReflectionTestUtils.setField(seller, "id", 1L);
        ReflectionTestUtils.setField(item, "id", 1L);
        ReflectionTestUtils.setField(item, "price", 1000);
        com.omegafrog.My.piano.app.web.domain.order.Order order =
                new com.omegafrog.My.piano.app.web.domain.order.Order(seller,
                        buyer, item, 0D, null);

        buyer.pay(order);
        Assertions.assertThat(buyer.isPurchased(item)).isTrue();
    }

    @Test
    void receiveCash() {
    }

    @Test
    void isPurchased() {
    }

    @Test
    void deleteUploadedVideoPost() {
    }

    @Test
    void addLikedLesson() {
    }

    @Test
    void isLikedLesson() {
    }

    @Test
    void dislikeLesson() {
    }

    @Test
    void unScrapLesson() {
    }
}