package com.omegafrog.My.piano.app.post.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.dto.CommentDTO;
import com.omegafrog.My.piano.app.dto.UpdatePostDto;
import com.omegafrog.My.piano.app.dto.WritePostDto;
import com.omegafrog.My.piano.app.exception.AuthorizationRequiredException;
import com.omegafrog.My.piano.app.post.entity.Comment;
import com.omegafrog.My.piano.app.post.entity.Post;
import com.omegafrog.My.piano.app.post.entity.PostRepository;
import com.omegafrog.My.piano.app.response.*;
import com.omegafrog.My.piano.app.user.entity.User;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;



    @PostMapping("")
    public JsonAPIResponse writePost(Authentication authentication, @RequestBody WritePostDto post) {
        try {
            User author = (User) authentication.getDetails();
            if (author == null) {
                return new APIInternalServerResponse("Internal server error");
            }

            Post entity = Post.builder()
                    .title(post.getTitle())
                    .content(post.getContent())
                    .author(author)
                    .build();

            Post saved = postRepository.save(entity);
            Map<String, Object> data = new HashMap<>();
            data.put("post", saved);
            return new APISuccessResponse("Write post success", objectMapper, data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse("Internal server error");
        }
    }

    @GetMapping("/{id}")
    public JsonAPIResponse findPost(@PathVariable Long id) {
        try{
            Post byId = postRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("cannot find post")
            );

            Map<String, Object> data = new HashMap<>();
            data.put("post", byId);
            return new APISuccessResponse("Find post success", objectMapper, data);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse("Internal server error");
        }catch (EntityNotFoundException e){
            e.printStackTrace();
            return new APIBadRequestResponse(e.getMessage());
        }
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updatePost(Authentication authentication,@PathVariable Long id, @RequestBody UpdatePostDto post) {
        try {
            User user = (User) authentication.getDetails();
            Post byId = postRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("cannot find post")
            );
            if (byId.getAuthor().equals(user)) {
                Post updated = byId.update(post);
                Post updatedEntity = postRepository.save(updated);
                Map<String, Object> data = new HashMap<>();
                data.put("post", updatedEntity.toDto());
                return new APISuccessResponse("update post success", objectMapper, data);
            } else {
                throw new ClientAuthorizationRequiredException("cannot update other user's post");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse("Internal server error");
        } catch (ClientAuthorizationRequiredException e) {
            e.printStackTrace();
            return new APIForbiddenResponse(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse deletePost(Authentication authentication,@PathVariable Long id) {
        Throwable ex=null;
        try {
            User user = (User) authentication.getDetails();
            Post founded = postRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("cannot find post")
            );
            if (user.equals(founded.getAuthor())) {
                postRepository.deleteById(founded.getId());
                return new APISuccessResponse("delete post success");
            }
            else
                throw new AuthorizationRequiredException("cannot delete other user's post");
        } catch (EntityNotFoundException e) {
            ex = e;
            return new APIBadRequestResponse(e.getMessage());
        } catch (AuthorizationRequiredException e) {
            ex = e;
            return new APIForbiddenResponse(e.getMessage());
        } finally {
            if(ex !=null)
                ex.printStackTrace();
        }
    }

    @PostMapping("/{id}/comment")
    public JsonAPIResponse addComment(Authentication authentication, @RequestBody CommentDTO dto, @PathVariable Long id){
        try{
            User loggedinUser = (User) authentication.getDetails();
            Post post = postRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("cannot find post.")
            );
            post.addComment(Comment.builder()
                    .author(loggedinUser)
                    .content(dto.getContent())
                    .build());
            Post saved = postRepository.save(post);
            List<Comment> comments = saved.getComments();
            Map<String, Object> data = new HashMap<>();
            data.put("comments", comments);
            return new APISuccessResponse("add comment success.", objectMapper, data);
        }catch (EntityNotFoundException e){
            e.printStackTrace();
            return new APIBadRequestResponse(e.getMessage());
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/comment/{comment-id}")
    public JsonAPIResponse deleteComment(Authentication authentication, @PathVariable Long id, @PathVariable(name = "comment-id")Long commentId){
        try{
            User user = (User) authentication.getDetails();
            Post post = postRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("cannot find post")
            );
            Comment foundedComment = post.getComments().stream().filter(comment -> comment.getId().equals(commentId)).findFirst()
                    .orElseThrow(
                            () -> new EntityNotFoundException("cannot find comment")
                    );
            if(isLoggedInUserWroteComment(user, foundedComment))
                throw new AuthorizationRequiredException("cannot delete other user's comment.");

            post.deleteComment(commentId);
            Post saved = postRepository.save(post);
            Map<String, Object> data = new HashMap<>();
            data.put("comments", saved.getComments());
            return new APISuccessResponse("delete comment success.", objectMapper, data);
        }catch (EntityNotFoundException | AuthorizationRequiredException e){
            e.printStackTrace();
            return new APIBadRequestResponse(e.getMessage());
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIInternalServerResponse(e.getMessage());
        }
    }

    private static boolean isLoggedInUserWroteComment(User user, Comment foundedComment) {
        return !foundedComment.getAuthor().getId().equals(user.getId());
    }
}
