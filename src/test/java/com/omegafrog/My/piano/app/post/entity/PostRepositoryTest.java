package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import org.assertj.core.api.Assertions;
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
    void saveNFindPostTest() {
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .viewCount(0)
                .likeCount(0)
                .author(new Author(0L, "author1", "none"))
                .createdAt(LocalDateTime.now())
                .build();
        Post saved = postRepository.save(post);
        Assertions.assertThat(saved).isEqualTo(post);
        Optional<Post> founded = postRepository.findById(saved.getId());
        Assertions.assertThat(founded.isPresent()).isTrue();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    void updatePostTest(){
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .viewCount(0)
                .likeCount(0)
                .author(new Author(0L, "author1", "none"))
                .createdAt(LocalDateTime.now())
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
    void deletePostTest(){
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .viewCount(0)
                .likeCount(0)
                .author(new Author(0L, "author1", "none"))
                .createdAt(LocalDateTime.now())
                .build();
        Post saved = postRepository.save(post);
        postRepository.deleteById(saved.getId());
        Optional<Post> founded = postRepository.findById(saved.getId());
        Assertions.assertThat(founded.isEmpty()).isTrue();
    }

}