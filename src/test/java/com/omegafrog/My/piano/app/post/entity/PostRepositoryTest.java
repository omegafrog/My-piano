package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {


    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글을 작성하고 조회할 수 있어야 한다")
    void saveNFindPostTest() {
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new Author("author1", "none"))
                .build();
        Post saved = postRepository.save(post);
        Assertions.assertThat(saved).isEqualTo(post);
        Optional<Post> founded = postRepository.findById(saved.getId());
        Assertions.assertThat(founded.isPresent()).isTrue();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    @DisplayName("게시글을 수정할 수 있어야 한다.")
    void updatePostTest(){
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new Author( "author1", "none"))
                .build();
        Post saved = postRepository.save(post);
        UpdatePostDto updatePostDto = UpdatePostDto.builder()
                .title("updated")
                .content("updatedContent")
                .viewCount(1)
                .likeCount(1)
                .build();
        Post updatedPost = post.update(updatePostDto);
        Post updated = postRepository.save(updatedPost);

        Assertions.assertThat(updated).isEqualTo(saved);
        Assertions.assertThat(updated.getContent()).isEqualTo("updatedContent");
    }
    @Test
    @DisplayName("게시글을 삭제할 수 있어야 한다")
    void deletePostTest(){
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new Author( "author1", "none"))
                .build();
        Post saved = postRepository.save(post);
        postRepository.deleteById(saved.getId());
        Optional<Post> founded = postRepository.findById(saved.getId());
        Assertions.assertThat(founded.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있어야 한다.")
    void addCommentTest(){
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new Author( "author1", "none"))
                .build();
        Post saved = postRepository.save(post);
        //when
        Comment comment = Comment.builder()
                .content("comment1")
                .author(new Author( "author1", "none"))
                .build();
        saved.addComment(comment);
        Post commentAdded = postRepository.save(saved);
        //then
        Assertions.assertThat(commentAdded.getComments().size()).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(commentAdded.getComments().get(0).getContent()).isEqualTo("comment1");
    }

    @Test
    @DisplayName("게시글의 댓글을 삭제할 수 있어야 한다.")
    void deleteCommentTest(){
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new Author( "author1", "none"))
                .build();
        Post saved = postRepository.save(post);
        Comment comment = Comment.builder()
                .content("comment1")
                .author(new Author( "author1", "none"))
                .build();
        saved.addComment(comment);
        Post commentAdded = postRepository.save(saved);
        //when
        commentAdded.deleteComment(commentAdded.getComments().get(0).getId());
        Post deletedComment = postRepository.save(commentAdded);

        //then
        Assertions.assertThat(deletedComment.getComments().size()).isEqualTo(0);
    }

}