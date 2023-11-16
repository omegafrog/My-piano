package com.omegafrog.My.piano.app.web.domain.user;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
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
    private List<Coupon> coupons;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "purchased_sheet",
            joinColumns = @JoinColumn(name = "AUTHOR_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<SheetPost> purchasedSheets = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "purchased_lesson",
            joinColumns = @JoinColumn(name = "AUTHOR_ID"),
            inverseJoinColumns = @JoinColumn(name = "LESSON_ID"))
    private List<SellableItem> purchasedLessons = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "scrapped_sheet",
            joinColumns = @JoinColumn(name = "AUTHOR_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<SellableItem> scrappedSheets = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "scrapped_lesson",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LESSON_ID"))
    private List<Lesson> scrappedLesson = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "AUTHOR_ID")
    private List<SellableItem> uploadedSheets = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "AUTHOR_ID")
    private List<Lesson> uploadedLessons = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Post> uploadedPosts = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<VideoPost> uploadedVideoPosts = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "followed",
            joinColumns = @JoinColumn(name = "FOLLOWER_ID"),
            inverseJoinColumns = @JoinColumn(name = "FOLLOWEE_ID")
    )
    private List<User> followed = new CopyOnWriteArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "liked_post",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "POST_ID"))
    private List<Post> likedPosts = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "liked_sheet_post",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_POST_ID"))
    private List<SheetPost> likedSheetPosts = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "liked_videoPost",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "VIDEO_POST_ID"))
    private List<VideoPost> likedVideoPosts = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "liked_lesson",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LESSON_ID"))
    private List<Lesson> likedLessons = new ArrayList<>();


    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Comment> wroteComments = new ArrayList<>();

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

    public void addUploadedVideoPost(VideoPost videoPost) {
        uploadedVideoPosts.add(videoPost);
        if (videoPost.getAuthor() != this) {
            videoPost.setAuthor(this);
        }
    }

    public boolean isScrappedLesson(Lesson lesson) {
        return scrappedLesson.stream().anyMatch(item -> item.equals(lesson));
    }

    public void scrapLesson(Lesson lesson) {
        if (isScrappedLesson(lesson))
            throw new EntityExistsException("이미 스크랩한 레슨입니다. id:" + lesson.getId());
        scrappedLesson.add(lesson);
    }

    public void addScrappedSheetPost(SheetPost sheetPost) {
        scrappedSheets.add(sheetPost);
    }

    public void addLikedSheetPost(SheetPost sheetPost) {
        likedSheetPosts.add(sheetPost);
        sheetPost.increaseLikedCount();
    }

    public void deleteLikedSheetPost(SheetPost sheetPost) {
        likedSheetPosts.remove(sheetPost);
        sheetPost.decreaseLikedCount();
    }

    public void deleteUploadedPost(Post post) {
        uploadedPosts.remove(post);
    }

    public void addLikePost(Post post) {
        likedPosts.add(post);
    }

    public void likeVideoPost(VideoPost videoPost) {
        if (likedVideoPosts.contains(videoPost))
            throw new EntityExistsException(ExceptionMessage.ENTITY_EXISTS);
        likedVideoPosts.add(videoPost);
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

    public User update(UpdateUserDto userDto) {
        this.name = userDto.getName();
        this.profileSrc = userDto.getProfileSrc();
        this.phoneNum = userDto.getPhoneNum();
        return this;
    }


    public boolean dislikePost(Long postId) {
        likedPosts.forEach(
                post -> {
                    if (post.getId().equals(postId)) post.decreaseLikedCount();
                }
        );
        return likedPosts.removeIf(post -> post.getId().equals(postId));
    }

    public void addCash(int cash) {
        this.cash += cash;
    }

    public void pay(Order order) throws PaymentException, ClassCastException {
        if (cash < order.getTotalPrice()) {
            throw new NotEnoughCashException("Cannot buy this item => cash:"
                    + cash + " < price:" + order.getTotalPrice());
            // 캐시 부족
        } else {
            cash -= order.getTotalPrice();
            order.setStatus(OrderStatus.PROGRESSING);
            if (order.getItem() instanceof Lesson)
                purchasedLessons.add(order.getItem());
            else if (order.getItem() instanceof SheetPost)
                purchasedSheets.add((SheetPost)order.getItem());
            else
                throw new ClassCastException("Cannot cast this class to child class.");
        }
    }

    public void receiveCash(int totalPrice) {
        cash += totalPrice;
    }

    /**
     * 유저 엔티티가 이미 구매한 상품인지 확인하는 함수
     * @param item 구매 여부를 확인할 상품 엔티티. SellableItem의 서브클래스의 인스턴스.
     * @return 구매한 상품이라면 true, 구매하지 않은 상품이라면 false
     */
    public boolean isPurchased(SellableItem item){
        List<? extends SellableItem> purchasedItemList;
        if (item instanceof SheetPost)
            purchasedItemList = purchasedSheets;
        else if (item instanceof Lesson)
            purchasedItemList = purchasedLessons;
        else throw new ClassCastException("Cannot find SellableItem collection");

        return purchasedItemList.stream().anyMatch(purchasedItem -> purchasedItem.equals(item));
    }

    public UserProfile getUserProfile() {
        return UserProfile.builder()
                .id(id)
                .name(name)
                .profileSrc(profileSrc)
                .loginMethod(loginMethod)
                .role(securityUser.getRole())
                .cash(cash)
                .build();
    }


    public void deleteUploadedVideoPost(VideoPost videoPost) {
        if (!uploadedVideoPosts.remove(videoPost))
            throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST);
    }

    public void addLikedLesson(Lesson lesson) {
        if (likedLessons.stream().anyMatch(item -> item.equals(lesson)))
            throw new EntityExistsException("이미 좋아요를 누른 글입니다.");
        likedLessons.add(lesson);
    }

    public boolean isLikedLesson(Lesson lesson) {
        return likedLessons.stream().anyMatch(item -> item.equals(lesson));
    }

    public void dislikeLesson(Lesson lesson) {
        likedLessons.remove(lesson);
    }

    public void unScrapLesson(Lesson lesson) {
        if (!isScrappedLesson(lesson))
            throw new EntityNotFoundException("스크랩하지 않은 레슨을 취소하려고 합니다." + lesson.getId());
        scrappedLesson.remove(lesson);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
