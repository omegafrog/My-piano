package com.omegafrog.My.piano.app.web.domain.relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_purchased_lesson")
@NoArgsConstructor
public class UserPurchasedLesson implements UserPurchasedItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonBackReference("purchased-lesson-user")
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "lesson_id")
	private Lesson lesson;

	@Builder
	public UserPurchasedLesson(User user, Lesson lesson) {
		this.user = user;
		this.lesson = lesson;
	}

	@Override
	public SellableItem getItem() {
		return lesson;
	}
}