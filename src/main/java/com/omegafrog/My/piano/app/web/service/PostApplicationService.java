package com.omegafrog.My.piano.app.web.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.ReturnPostListDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.enums.PostType;
import com.omegafrog.My.piano.app.web.event.EventPublisher;
import com.omegafrog.My.piano.app.web.event.PostCreatedEvent;
import com.omegafrog.My.piano.app.web.event.PostDeletedEvent;
import com.omegafrog.My.piano.app.web.event.PostUpdatedEvent;
import com.omegafrog.My.piano.app.web.exception.message.ExceptionMessage;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final AuthenticationUtil authenticationUtil;
	private final PostViewCountRepository postViewCountRepository;
	private final EventPublisher eventPublisher;

	public PostDto writePost(PostRegisterDto post) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		User user = userRepository.findById(loggedInUser.getId())
				.orElseThrow(
						() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + loggedInUser.getId()));

		Post build = Post.builder()
				.title(post.getTitle())
				.content(post.getContent())
				.author(user)
				.type(PostType.COMMON)
				.build();
		Post saved = postRepository.save(build);
		user.addUploadedPost(saved);

		// Publish post created event
		PostCreatedEvent event = new PostCreatedEvent(
				saved.getId(),
				saved.getTitle(),
				saved.getContent(),
				saved.getType().toString(),
				saved.getAuthor().getId(),
				saved.getAuthor().getName());
		eventPublisher.publishPostCreated(event);

		return saved.toDto();
	}

	public PostDto findPostById(Long id) {
		Post founded = postRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST + id));
		int incrementedViewCount = postViewCountRepository.incrementViewCount(founded);
		PostDto postDto = new PostDto(founded, founded.getAuthor());
		postDto.setViewCount(incrementedViewCount);
		return postDto;
	}

	public PostDto updatePost(Long id, UpdatePostDto updatePostDto) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		Post post = getPostById(id);
		if (post.getAuthor().equals(loggedInUser)) {
			Post updatedPost = post.update(updatePostDto);

			// Publish post updated event
			PostUpdatedEvent event = new PostUpdatedEvent(
					updatedPost.getId(),
					updatedPost.getTitle(),
					updatedPost.getContent(),
					updatedPost.getType().toString(),
					updatedPost.getAuthor().getId(),
					updatedPost.getAuthor().getName());
			eventPublisher.publishPostUpdated(event);

			return updatedPost.toDto();
		} else
			throw new AccessDeniedException("Cannot update other user's post");
	}

	public void deletePost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		Post post = getPostById(id);
		if (post.getAuthor().equals(loggedInUser)) {
			postRepository.deleteById(id);

			// Publish post deleted event
			PostDeletedEvent event = new PostDeletedEvent(id);
			eventPublisher.publishPostDeleted(event);
		} else
			throw new AccessDeniedException("Cannot delete other user's post");
	}

	private Post getPostById(Long id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST + id));
	}

	public void likePost(Long postId) {
		User user = authenticationUtil.getLoggedInUser();
		Post post = getPostById(postId);
		post.increaseLikedCount();
		user.likePost(post);
	}

	public void dislikePost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		Post dislikedPost = getPostById(id);
		User founded = userRepository.findById(loggedInUser.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cannot find User entity."));
		founded.dislikePost(dislikedPost);
	}

	public boolean isLikedPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		User founded = userRepository.findById(loggedInUser.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. "));
		return !founded.getLikedPosts().stream().filter(post -> post.getId().equals(id)).findFirst().isEmpty();
	}

	public ReturnPostListDto findPosts(Pageable pageable) {
		Long count = postRepository.count();
		List<PostListDto> postList = postRepository.findAll(pageable, Sort.by(Sort.Direction.DESC, "createdAt"))
				.stream()
				.map(PostListDto::new)
				.toList();
		return new ReturnPostListDto(count, postList);
	}
}
