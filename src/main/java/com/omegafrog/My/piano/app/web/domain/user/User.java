package com.omegafrog.My.piano.app.web.domain.user;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.utils.exception.AlreadyScrappedEntityException;
import com.omegafrog.My.piano.app.utils.exception.AlreadyLikedEntityException;
import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.relation.*;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.ChangeUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.utils.exception.payment.NotEnoughCashException;
import com.omegafrog.My.piano.app.utils.exception.payment.PaymentException;
import com.omegafrog.My.piano.app.web.vo.user.AlarmProperties;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "person")
@NoArgsConstructor
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @NotBlank(message = "name cannot be null")
    private String name;

    @Email
    @NotBlank(message = "email cannot be null")
    @Column(unique = true)
    private String email;

    @NotNull
    private LoginMethod loginMethod;

    @Value("${user.baseProfileSrc}")
    private String profileSrc;
    @PositiveOrZero
    private int point;
    @PositiveOrZero
    private int cash;

    @Nullable
    private PhoneNum phoneNum;
    @NotNull
    private AlarmProperties alarmProperties;

    @OneToOne(mappedBy = "user")
    private SecurityUser securityUser;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "USER_ID")
    @NotNull
    private Cart cart;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "owner")
    private List<Coupon> coupons = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<UserPurchasedSheetPost> purchasedSheets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<UserPurchasedLesson> purchasedLessons = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<UserScrappedSheetPost> scrappedSheetPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private final List<UserScrappedLesson> scrappedLesson = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "author")
    private final List<SheetPost> uploadedSheetPosts = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "author")
    private final List<Lesson> uploadedLessons = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private final List<Post> uploadedPosts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private final List<VideoPost> uploadedVideoPosts = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<FollowedUser> followed = new CopyOnWriteArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST})
    private final List<UserLikedPost> likedPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private final List<UserLikedSheetPost> likedSheetPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private final List<UserLikedVideoPost> likedVideoPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private final List<UserLikedLesson> likedLessons = new ArrayList<>();


    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<Comment> wroteComments = new ArrayList<>();

    public int chargeCash(int cash) {
        this.cash += cash;
        return this.cash;
    }

    public void addUploadedPost(Post post) {
        uploadedPosts.add(post);
        if (post.getAuthor() != this) {
            post.setAuthor(this);
        }
    }

    public void addUploadedLesson(Lesson lesson) {
        uploadedLessons.add(lesson);
        if (lesson.getAuthor() != this)
            lesson.setAuthor(this);
    }

    public void addUploadedVideoPost(VideoPost videoPost) {
        uploadedVideoPosts.add(videoPost);
        if (videoPost.getAuthor() != this) {
            videoPost.setAuthor(this);
        }
    }

    public boolean isScrappedLesson(Lesson lesson) {
        return scrappedLesson.stream().anyMatch(item -> item.getLesson().equals(lesson));
    }

    public void scrapLesson(Lesson lesson) {
        if (isScrappedLesson(lesson))
            throw new EntityExistsException("이미 스크랩한 레슨입니다. id:" + lesson.getId());
        scrappedLesson.add(
                UserScrappedLesson.builder()
                        .lesson(lesson)
                        .user(this)
                        .build());
    }

    public void unScrapLesson(Lesson lesson) {
        if (!isScrappedLesson(lesson))
            throw new EntityNotFoundException("스크랩하지 않은 레슨을 취소하려고 합니다." + lesson.getId());
        scrappedLesson.removeIf(l -> l.getLesson().equals(lesson));
    }

    public void scrapSheetPost(SheetPost sheetPost) {
        if(isScrappedSheetPost(sheetPost))
            throw new AlreadyScrappedEntityException("이미 스크랩한 entity입니다.");
        scrappedSheetPosts.add(
                UserScrappedSheetPost.builder()
                        .sheetPost(sheetPost)
                        .user(this)
                        .build());
    }

    public boolean isScrappedSheetPost(SheetPost sheetPost) {
        return scrappedSheetPosts.stream().anyMatch(item -> item.getSheetPost().equals(sheetPost));
    }

    public void likeSheetPost(SheetPost sheetPost) {
        if(isLikedSheetPost(sheetPost))
            throw new AlreadyLikedEntityException();
        likedSheetPosts.add(UserLikedSheetPost.builder()
                .user(this)
                .sheetPost(sheetPost)
                .build());
        sheetPost.increaseLikedCount();
    }

    private boolean isLikedSheetPost(SheetPost sheetPost) {
        return likedSheetPosts.stream().anyMatch(item -> item.getSheetPost().equals(sheetPost));
    }

    public void dislikeSheetPost(SheetPost sheetPost) {
        boolean removed = likedSheetPosts.removeIf(item -> item.getSheetPost().equals(sheetPost));
        if(!removed) throw new EntityNotFoundException("sheetPost entity를 찾을 수 없습니다.id:" + sheetPost.getId());
        sheetPost.decreaseLikedCount();
    }

    public void deleteUploadedPost(Post post) {
        uploadedPosts.remove(post);
    }


    public void likeVideoPost(VideoPost videoPost) {
        if (likedVideoPosts.contains(videoPost))
            throw new EntityExistsException(ExceptionMessage.ENTITY_EXISTS);
        likedVideoPosts.add(UserLikedVideoPost.builder()
                .videoPost(videoPost)
                .user(this)
                .build());
        videoPost.increaseLikedCount();
    }

    public void dislikeVideoPost(VideoPost videoPost) {
        if (!likedVideoPosts.removeIf(post -> post.equals(videoPost)))
            throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST);
    }

    public void addWroteComments(Comment comment) {
        wroteComments.add(comment);
    }

    public void deleteWroteComments(Comment comment, User loggedInUser) {
        boolean isCommentRemoved = wroteComments.removeIf(
                element -> {
                    if (element.equals(comment)) {
                        if (element.getAuthor().equals(loggedInUser))
                            return true;
                        else throw new AccessDeniedException("Cannot delete other user's comment.");
                    } else return false;
                }
        );
        if (!isCommentRemoved) throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT);
    }

    @Builder
    public User(String name, String email, Cart cart, LoginMethod loginMethod, String profileSrc, PhoneNum phoneNum, int cash) {

        this.email = email;
        this.name = name;
        this.alarmProperties = new AlarmProperties();
        this.loginMethod = loginMethod;
        this.profileSrc = profileSrc;
        this.phoneNum = phoneNum;
        this.cart = cart;
        this.cash = cash;
    }

    public User update(ChangeUserDto userDto) {
        this.name = userDto.getName();
        this.profileSrc = userDto.getProfileSrc();
        this.phoneNum = new PhoneNum(userDto.getPhoneNum());
        return this;
    }

    public void likePost(Post post) {
        assert likedPosts.stream().anyMatch(p -> p.getPost().equals(post));
        likedPosts.add(UserLikedPost.builder()
                .post(post)
                .user(this)
                .build());
    }

    public void dislikePost(Post dislikedPost) {
        likedPosts.removeIf(item -> item.getPost().equals(dislikedPost));
    }

    public void addCash(int cash) {
        this.cash += cash;
    }

    public void pay(Order order) throws PaymentException, ClassCastException {
        if (cash < order.getTotalPrice()) {
            throw new NotEnoughCashException("Cannot buy this item => cash:"
                    + cash + " < price:" + order.getTotalPrice());
            // 캐시 부족
        }

        cash -= order.getTotalPrice();
        order.setStatus(OrderStatus.IN_PROGRESS);
        if (order.getItem() instanceof Lesson item)
            purchasedLessons.add(
                    UserPurchasedLesson.builder()
                            .lesson(item)
                            .user(this)
                            .build()
            );
        else if (order.getItem() instanceof SheetPost item)
            purchasedSheets.add(
                    UserPurchasedSheetPost.builder()
                            .sheetPost(item)
                            .user(this)
                            .build()
            );
        else
            throw new ClassCastException("Cannot cast this class to child class.");
    }

    public void receiveCash(int totalPrice) {
        cash += totalPrice;
    }

    /**
     * 유저 엔티티가 이미 구매한 상품인지 확인하는 함수
     * @param item 구매 여부를 확인할 상품 엔티티. SellableItem의 서브클래스의 인스턴스.
     * @return 구매한 상품이라면 true, 구매하지 않은 상품이라면 false
     */
    public boolean isPurchased(SellableItem item) {
        List<? extends PurchasedSheetPost> purchasedItemList;
        if (item instanceof SheetPost)
            purchasedItemList = purchasedSheets;
        else if (item instanceof Lesson)
            purchasedItemList = purchasedLessons;
        else throw new ClassCastException("Cannot find SellableItem collection");

        return purchasedItemList.stream().anyMatch(purchasedItem -> purchasedItem.equals(item));
    }

    public UserInfo getUserInfo() {
        return UserInfo.builder()
                .id(id)
                .name(name)
                .username(securityUser.getUsername())
                .email(email)
                .profileSrc(profileSrc)
                .loginMethod(loginMethod)
                .role(securityUser.getRole())
                .enabled(!securityUser.isLocked())
                .cash(cash)
                .build();
    }
    public UserProfileDto getUserProfileDto() {
        return new UserProfileDto(id, securityUser.getUsername(), profileSrc);
    }


    public void deleteUploadedVideoPost(VideoPost videoPost) {
        if (!uploadedVideoPosts.remove(videoPost))
            throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST);
    }

    public void likeLesson(Lesson lesson) {
        if (likedLessons.stream().anyMatch(item -> item.getLesson().equals(lesson)))
            throw new EntityExistsException("이미 좋아요를 누른 글입니다.");
        likedLessons.add(UserLikedLesson.builder()
                .lesson(lesson)
                .user(this)
                .build());
    }

    public boolean isLikedLesson(Lesson lesson) {
        return likedLessons.stream().anyMatch(item -> item.getLesson().equals(lesson));
    }

    public void dislikeLesson(Lesson lesson) {
        likedLessons.removeIf(item->item.getLesson().equals(lesson));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
