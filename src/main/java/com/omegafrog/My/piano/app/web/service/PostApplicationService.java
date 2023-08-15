package com.omegafrog.My.piano.app.web.service;


import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    public PostDto writePost(PostRegisterDto post, User author) {
        User user = userRepository.findById(author.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + author.getId()));
        Post build = Post.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .author(user)
                .build();
        Post saved = postRepository.save(build);
        user.getUploadedPosts().add(saved);
        return saved.toDto();
    }

    public PostDto findPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST + id)).toDto();
    }

    public PostDto updatePost(Long id, UpdatePostDto updatePostDto, User loggedInUser) {
        Post post = getPostById(id);
        if (post.getAuthor().equals(loggedInUser)) {
            Post updated = post.update(updatePostDto);
            return postRepository.save(updated).toDto();
        } else throw new AccessDeniedException("Cannot update other user's post");
    }

    public void deletePost(Long id, User loggedInUser) {
        Post post = getPostById(id);
        if (post.getAuthor().equals(loggedInUser)) {
            postRepository.deleteById(id);
        } else throw new AccessDeniedException("Cannot delete other user's post");
    }

    public List<CommentDto> addComment(Long id, User loggedInUser, CommentDto dto) {
        Comment build = Comment.builder().author(loggedInUser).content(dto.getContent()).build();
        Post post = getPostById(id);
        post.addComment(build);
        Post saved = postRepository.save(post);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }

    private Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST+ id));
    }

    public List<CommentDto> deleteComment(Long id, Long commentId, User loggedInUser) {
        Post post = getPostById(id);
        if (!isCommentRemoved(commentId, loggedInUser, post))
            throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT + commentId);
        Post saved = postRepository.save(post);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }

    private static boolean isCommentRemoved(Long commentId, User loggedInUser, Post post) {
        return post.getComments().removeIf(comment -> {
            if (comment.getId().equals(commentId)) {
                if (comment.getAuthor().equals(loggedInUser)) return true;
                else throw new AccessDeniedException("Cannot delete other user's comment.");
            } else return false;
        });
    }

    public void likePost(Long postId, User user) {
        User byId = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER + user.getId()));
        Post post = getPostById(postId);
        post.increaseLikedCount();
        byId.addLikePost(post);
        userRepository.save(byId);
    }

    public void dislikePost(Long id, User loggedInUser){
        getPostById(id);
        if (!loggedInUser.dislikePost(id)) {
            throw new EntityNotFoundException("Cannot find post entity that you liked.");
        }
    }
}
