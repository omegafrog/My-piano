package com.omegafrog.My.piano.app.web.domain.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.ReturnCommentDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonBackReference("comment-user")
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User author;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt = LocalDateTime.now();

	private String content;

	private int likeCount;

	@JsonManagedReference("comment-parent")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
	private final List<Comment> replies = new ArrayList<>();

	@JsonBackReference("comment-parent")
	@ManyToOne
	@JoinColumn(name = "PARENT_ID")
	private Comment parent;

	@JsonBackReference("comment-target")
	@ManyToOne
	private Article target;

	public void setTarget(Article article) {
		target = article;
	}

	public void increaseLikeCount() {
		likeCount++;
	}

	public void decreaseLikeCount() {
		likeCount--;
	}

	@Builder
	public Comment(Long id, User author, String content, Article target, Comment parent) {
		this.id = id;
		this.author = author;
		this.content = content;
		this.likeCount = 0;
		this.parent = parent;
	}

	public CommentDto toDto() {
		return CommentDto.builder()
			.id(id)
			.author(author.getUserInfo())
			.content(content)
			.createdAt(createdAt)
			.likeCount(likeCount)
			.build();
	}

	public ReturnCommentDto toReturnCommentDto() {
		return ReturnCommentDto.builder()
			.id(id)
			.content(content)
			.targetId(target.getId())
			.likeCount(likeCount)
			.author(author.getUserInfo())
			.createdAt(createdAt)
			.build();
	}

	public void addReply(Comment saved) {
		replies.add(saved);
	}

	public void setAuthor(User author) {
		this.author = author;
	}
}
