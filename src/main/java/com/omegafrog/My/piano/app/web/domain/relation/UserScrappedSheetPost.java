package com.omegafrog.My.piano.app.web.domain.relation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
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
@Table(name = "user_scrapped_sheet_post")
@NoArgsConstructor
public class UserScrappedSheetPost implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonBackReference("scrapped-sheetpost-user")
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@JsonBackReference("user-scrapped-sheetpost")
	@ManyToOne
	@JoinColumn(name = "sheet_post_id")
	private SheetPost sheetPost;

	@Builder
	public UserScrappedSheetPost(User user, SheetPost sheetPost) {
		this.user = user;
		this.sheetPost = sheetPost;
	}
}