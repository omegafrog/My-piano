package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor
@Getter
public class SellableItem extends Article {

    protected int price;

    protected Double discountRate = 0d;

    public SellableItem(User author, String title, String content, int price) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.price = price;
    }

    public void updatePrice(int price){
        this.price = price;
    }
    public void updateDiscountRate(Double discountRate){ this.discountRate = discountRate;}
}
