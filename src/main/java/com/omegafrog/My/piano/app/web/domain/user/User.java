package com.omegafrog.My.piano.app.web.domain.user;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.post.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.exception.payment.NotEnoughCashException;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import com.omegafrog.My.piano.app.web.vo.user.AlarmProperties;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "`USER`")
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
    private String email;

    @NotNull
    private LoginMethod loginMethod;

    @Value("{user.baseProfileSrc}")
    private String profileSrc;
    @PositiveOrZero
    private int point;
    @PositiveOrZero
    private int cash;

    @NotNull
    private PhoneNum phoneNum;
    @NotNull
    private AlarmProperties alarmProperties;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "USER_ID")
    @NotNull
    private Cart cart;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "purchased_sheet",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<SheetPost> purchasedSheets = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "purchased_lesson",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LESSON_ID"))
    private List<Lesson> purchasedLessons = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "scrapped_sheet",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<SheetPost> scrappedSheets = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "USER_ID")
    private List<SheetPost> uploadedSheets = new ArrayList<>();


    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "USER_ID")
    private List<Post> uploadedPosts = new ArrayList<>();

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

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Comment> writedComments = new ArrayList<>();

    // TODO : USER에서 Cart를 팩토리로 생성해보자.
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

    public void addLikePost(Post post){
        likedPosts.add(post);
    }

    public boolean dislikePost(Long postId){
        likedPosts.forEach(
                post -> { if( post.getId().equals(postId)) post.decreaseLikedCount();}
        );
        return likedPosts.removeIf(post -> post.getId().equals(postId));
    }

    public void addCash(int cash){
        this.cash += cash;
    }

    public void pay(Order order) throws PaymentException {
        if (cash < order.getTotalPrice()) {
            throw new NotEnoughCashException("Cannot buy this item => cash:"
                    + cash + " < price:" + order.getTotalPrice());
            // 캐시 부족
        } else {
            cash -= order.getTotalPrice();
            if (order.getItem() instanceof Lesson)
                purchasedLessons.add((Lesson) order.getItem());
            else if (order.getItem() instanceof SheetPost)
                purchasedSheets.add((SheetPost) (order.getItem()));
            else
                throw new ClassCastException("Cannot cast this class to child class.");
        }
    }

    public void receiveCash(int totalPrice) {
        cash += totalPrice;
    }

    public UserProfile getUserProfile() {
        return UserProfile.builder()
                .id(id)
                .name(name)
                .profileSrc(profileSrc)
                .build();
    }

    public void addLikedPost(Post post){
        post.increaseLikedCount();
        likedPosts.add(post);
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
