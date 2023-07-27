package com.omegafrog.My.piano.app.web.service;


import com.omegafrog.My.piano.app.web.domain.post.Comment;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostDto writePost(PostRegisterDto post, User author) {
        Post build = Post.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .author(author)
                .build();
        return postRepository.save(build).toDto();
    }



    public PostDto findPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find post entity : " + id))
                .toDto();
    }

    public PostDto updatePost(Long id, UpdatePostDto updatePostDto, User loggedInUser)
            throws AccessDeniedException {
        Post post = getPostById(id);
        if (post.getAuthor().equals(loggedInUser)) {
            Post updated = post.update(updatePostDto);
            return postRepository.save(updated).toDto();
        } else throw new AccessDeniedException("Cannot update other user's post");
    }

    public void deletePost(Long id, User loggedInUser) {
        Post post = getPostById(id);

        if(post.getAuthor().equals(loggedInUser)){
            postRepository.deleteById(id);
        } else throw new AccessDeniedException("Cannot delete other user's post");
    }

    public List<CommentDto> addComment(Long id, User loggedInUser, CommentDto dto) {
        Comment build = Comment.builder()
                .author(loggedInUser)
                .content(dto.getContent())
                .build();
        Post post = getPostById(id);
        post.addComment(build);
        Post saved = postRepository.save(post);
        return saved.getComments().stream().map(Comment::toDto).collect(Collectors.toList());
    }

    private Post getPostById(Long id) throws EntityNotFoundException {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find post entity : " + id));
    }

    public List<CommentDto> deleteComment(Long id, Long commentId, User loggedInUser) {
        Post post = getPostById(id);
        post.getComments().removeIf(
                comment ->
                        comment.getId().equals(commentId) && comment.getAuthor().equals(loggedInUser)
        );
        Post saved = postRepository.save(post);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }
    public void likePost(Long postId, User user) {
        User byId = userRepository.findById(user.getId()).get();
        Post post = getPostById(postId);
        post.increaseLikedCount();
        byId.addLikePost(post);
        userRepository.save(byId);
//        postRepository.save(post);
    }

    public void dislikePost(Long id, User loggedInUser) throws EntityNotFoundException{
        Post post = getPostById(id);
        if(!loggedInUser.dislikePost(id)){
            throw new EntityNotFoundException("Cannot find post entity that you liked.");
        }
    }
}
