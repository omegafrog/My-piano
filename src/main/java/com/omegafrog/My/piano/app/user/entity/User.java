package com.omegafrog.My.piano.app.user.entity;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.lesson.entity.Lesson;
import com.omegafrog.My.piano.app.post.entity.Post;
import com.omegafrog.My.piano.app.sheet.Sheet;
import com.omegafrog.My.piano.app.user.dto.UpdateUserDto;
import com.omegafrog.My.piano.app.user.vo.AlarmProperties;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "`USER`")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    @Getter
    private Long id;
    private String name;

    private String email;

    private LoginMethod loginMethod;

    @Value("{user.baseProfileSrc}")
    private String profileSrc;
    private int point;
    private int cash;
    private PhoneNum phoneNum;
    private AlarmProperties alarmProperties;

    @OneToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "USER_ID")
    private Cart cart;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "purchased_sheet",
            joinColumns = @JoinColumn(name="USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<Sheet> purchasedSheets = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "purchased_lesson",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LESSON_ID"))
    private List<Lesson> purchasedLessons = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "scrapped_sheet",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SHEET_ID"))
    private List<Sheet> scrappedSheets = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private List<Sheet> uploadedSheets = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    // TODO : USER에서 Cart를 팩토리로 생성해보자.
    @Builder
    public User(String name, Cart cart, LoginMethod loginMethod, String profileSrc, PhoneNum phoneNum) {
        this.name = name;
        this.loginMethod = loginMethod;
        this.profileSrc = profileSrc;
        this.phoneNum = phoneNum;
        this.cart = cart;
    }

    public User update(UpdateUserDto userDto){
        this.name = userDto.getName();
        this.profileSrc = userDto.getProfileSrc();
        this.phoneNum = userDto.getPhoneNum();
        return this;
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
