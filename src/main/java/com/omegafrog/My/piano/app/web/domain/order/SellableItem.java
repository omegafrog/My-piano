package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
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

	public void updatePrice(int price) {
		this.price = price;
	}

	public void updateDiscountRate(Double discountRate) {
		this.discountRate = discountRate;
	}
}
