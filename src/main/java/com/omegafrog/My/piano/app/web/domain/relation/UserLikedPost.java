package com.omegafrog.My.piano.app.web.domain.relation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.post.Post;
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
@Table(name = "user_liked_post")
@NoArgsConstructor
public class UserLikedPost implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@JsonBackReference("liked-post-user")
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Builder
	public UserLikedPost(Post post, User user) {
		this.post = post;
		this.user = user;
	}

}