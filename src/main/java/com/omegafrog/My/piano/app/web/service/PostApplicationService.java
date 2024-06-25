package com.omegafrog.My.piano.app.web.service;


import com.omegafrog.My.piano.app.web.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostViewCountRepository postViewCountRepository;

    public PostDto writePost(PostRegisterDto post, User author) {
        User user = userRepository.findById(author.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + author.getId()));
        Post build = Post.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .author(user)
                .build();
        Post saved = postRepository.save(build);
        user.addUploadedPost(saved);
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

    public PostDto updatePost(Long id, UpdatePostDto updatePostDto, User loggedInUser) {
        Post post = getPostById(id);
        if (post.getAuthor().equals(loggedInUser)) {
            return post.update(updatePostDto).toDto();

        } else throw new AccessDeniedException("Cannot update other user's post");
    }

    public void deletePost(Long id, User loggedInUser) {
        Post post = getPostById(id);
        if (post.getAuthor().equals(loggedInUser)) {
            postRepository.deleteById(id);
        } else throw new AccessDeniedException("Cannot delete other user's post");
    }
    private Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST + id));
    }

    public void likePost(Long postId, User user) {
        User byId = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + user.getId()));
        Post post = getPostById(postId);
        post.increaseLikedCount();
        byId.likePost(post);
    }
    public void dislikePost(Long id, User loggedInUser){
        Post dislikedPost = getPostById(id);
        User founded = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity."));
        founded.dislikePost(dislikedPost);
    }
    public boolean isLikedPost(Long id, User loggedInUser){
        User founded = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find User entity. "));
        return !founded.getLikedPosts().stream().filter(post -> post.getId().equals(id)).findFirst().isEmpty();
    }

    public ReturnPostListDto findPosts(Pageable pageable) {
        Long count = postRepository.count();
        List<PostListDto> postList = postRepository.findAll(pageable, Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(PostListDto::new).toList();
        return new ReturnPostListDto(count, postList);
    }
}
