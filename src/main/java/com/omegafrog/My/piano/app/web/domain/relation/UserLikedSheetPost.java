package com.omegafrog.My.piano.app.web.domain.relation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_liked_sheet_post")
@NoArgsConstructor
public class UserLikedSheetPost implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonBackReference("liked-sheetpost-user")
	@ManyToOne
	private User user;
	@JsonBackReference("liked-sheetpost-sheetpost")
	@ManyToOne
	private SheetPost sheetPost;

	@Builder
	public UserLikedSheetPost(User user, SheetPost sheetPost) {
		this.user = user;
		this.sheetPost = sheetPost;
	}
}