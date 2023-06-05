package com.omegafrog.My.piano.app.dto;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.lesson.entity.Lesson;
import com.omegafrog.My.piano.app.post.entity.Post;
import com.omegafrog.My.piano.app.sheet.entity.Sheet;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.AlarmProperties;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import lombok.Builder;

import java.util.List;

public class UserDTO {
    private Long id;
    private String name;

    private String email;

    private LoginMethod loginMethod;

    private String profileSrc;
    private int point;

    private int cash;
    private PhoneNum phoneNum;
    private AlarmProperties alarmProperties;

    private Cart cart;

    private List<Sheet> purchasedSheets;

    private List<Lesson> purchasedLessons;

    private List<Sheet> scrappedSheets;

    private List<Sheet> uploadedSheets;

    private List<Post> uploadedPosts;

    private List<User> followed;
    private List<Post> likedPosts;



    @Builder
    public UserDTO(Long id, String name, String email, LoginMethod loginMethod, String profileSrc, int point, int cash, PhoneNum phoneNum, AlarmProperties alarmProperties, Cart cart, List<Sheet> purchasedSheets, List<Lesson> purchasedLessons, List<Sheet> scrappedSheets, List<Sheet> uploadedSheets, List<Post> uploadedPosts, List<User> followed, List<Post> likedPosts) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.loginMethod = loginMethod;
        this.profileSrc = profileSrc;
        this.point = point;
        this.cash = cash;
        this.phoneNum = phoneNum;
        this.alarmProperties = alarmProperties;
        this.cart = cart;
        this.purchasedSheets = purchasedSheets;
        this.purchasedLessons = purchasedLessons;
        this.scrappedSheets = scrappedSheets;
        this.uploadedSheets = uploadedSheets;
        this.uploadedPosts = uploadedPosts;
        this.followed = followed;
        this.likedPosts = likedPosts;
    }
}
