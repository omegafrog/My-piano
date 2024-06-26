package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
@NoArgsConstructor
@Getter
public class SellableItem extends Article {

    @NotNull
    protected Integer price;

    protected Double discountRate = 0d;


    public SellableItem(User author, String title, String content, Integer price) {
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
