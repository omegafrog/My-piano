package com.omegafrog.My.piano.app.web.service;


import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService implements CommentHandler {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

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
        founded.increaseViewCount();
        return new PostDto(founded, founded.getAuthor());
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
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST+ id));
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

    @Override
    public List<CommentDto> addComment(Long id,  RegisterCommentDto dto,User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        Comment build = Comment.builder().author(user).content(dto.getContent()).build();
        Comment savedComment = commentRepository.save(build);
        Post post = getPostById(id);
        post.addComment(savedComment);
        Post saved = postRepository.save(post);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }

    @Override
    public List<CommentDto> deleteComment(Long id, Long commentId, User loggedInUser) {
        Post post = getPostById(id);
        if (!isCommentRemoved(commentId, loggedInUser, post))
            throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT + commentId);
        return post.getComments().stream().map(Comment::toDto).toList();
    }

    private static boolean isCommentRemoved(Long commentId, User loggedInUser, Post post) {
        return post.getComments().removeIf(comment -> {
            if (comment.getId().equals(commentId)) {
                if (comment.getAuthor().equals(loggedInUser)) return true;
                else throw new AccessDeniedException("Cannot delete other user's comment.");
            } else return false;
        });
    }

    @Override
    public void likeComment(Long articleId, Long commentId) {
        Post post = postRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST));
        Comment foundedComment = post.getComments().stream().filter(comment -> comment.getId().equals(commentId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT));
        foundedComment.increaseLikeCount();
    }

    @Override
    public void dislikeComment(Long articleId, Long commentId) {
        Post post = postRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST));
        Comment foundedComment = post.getComments().stream().filter(comment -> comment.getId().equals(commentId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT));
        foundedComment.decreaseLikeCount();
    }

    @Override
    public Page<CommentDto> getComments(Long articleId, Pageable pageable) {
        Post post = postRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST));
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int toIdx = (int)offset+pageSize;
        if (toIdx > post.getComments().size()) toIdx = post.getComments().size();
        return PageableExecutionUtils.getPage(
                post.getComments().subList((int) offset, toIdx).stream().map(Comment::toDto).toList(),
                pageable,
                () ->post.getComments().size());
    }

    @Override
    public CommentDto replyComment(Long id, Long commentId, String replyContent, User loggedInUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_POST));
        Comment comment = post.getComments().stream().filter(item -> item.getId().equals(commentId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Cannot find comment entity : " + commentId));
        commentRepository.findById(commentId)
                        .orElseThrow(()->new EntityNotFoundException("Cannot find comment entity : " + commentId));
        Comment reply = Comment.builder().content(replyContent)
                .author(loggedInUser)
                .build();
        Comment saved = commentRepository.save(reply);
        comment.addReply(saved);
        return saved.toDto();
    }

    public ReturnPostListDto findPosts(Pageable pageable) {
        Long count = postRepository.count();
        List<PostListDto> postList = postRepository.findAll(pageable, Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(PostListDto::new).toList();
        return new ReturnPostListDto(count, postList);
    }
}
