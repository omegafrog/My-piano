package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdateVideoPostDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VideoPostRepositoryTest {

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Test
    @DisplayName("비디오 커뮤니티 게시글을 추가하고 조회할 수 있어야 한다.")
    void saveNFindTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new Author("author", "none"))
                .build();
        //when
        VideoPost saved = videoPostRepository.save(post);
        //then
        Assertions.assertThat(saved).isEqualTo(post);
    }

    @Test
    @DisplayName("비디오 커뮤니티 게시글을 수정할 수 있어야 한다.")
    void updateTest(){
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new Author("author", "none"))
                .build();
        VideoPost saved = videoPostRepository.save(post);
        //when
        String changedTitle = "changedTitle";
        String changedContent = "changedContent";
        String changedUrl = "changedUrl";
        UpdateVideoPostDto dto = UpdateVideoPostDto.builder()
                .title(changedTitle)
                .content(changedContent)
                .videoUrl(changedUrl)
                .build();
        VideoPost updated = saved.update(dto);
        VideoPost updatedVideoPost = videoPostRepository.save(updated);
        //then
        Assertions.assertThat(updatedVideoPost).isEqualTo(saved);
        Assertions.assertThat(updatedVideoPost.getTitle())
                .isEqualTo(changedTitle);
        Assertions.assertThat(updatedVideoPost.getContent())
                .isEqualTo(changedContent);
        Assertions.assertThat(updatedVideoPost.getVideoUrl())
                .isEqualTo(changedUrl);
    }
    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있어야 한다.")
    void addCommentTest(){
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new Author("author", "none"))
                .build();
        VideoPost saved = videoPostRepository.save(post);
        //when
        Comment comment = Comment.builder()
                .content("comment1")
                .author(new Author( "author1", "none"))
                .build();
        saved.addComment(comment);
        VideoPost commentAdded = videoPostRepository.save(saved);
        //then
        Assertions.assertThat(commentAdded.getComments().size()).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(commentAdded.getComments().get(0).getContent()).isEqualTo("comment1");
    }

    @Test
    @DisplayName("게시글의 댓글을 삭제할 수 있어야 한다.")
    void deleteCommentTest(){
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new Author("author", "none"))
                .build();
        VideoPost saved = videoPostRepository.save(post);
        Comment comment = Comment.builder()
                .content("comment1")
                .author(new Author( "author1", "none"))
                .build();
        saved.addComment(comment);
        VideoPost commentAdded = videoPostRepository.save(saved);
        //when
        commentAdded.deleteComment(commentAdded.getComments().get(0).getId());
        VideoPost deletedComment = videoPostRepository.save(commentAdded);

        //then
        Assertions.assertThat(deletedComment.getComments().size()).isEqualTo(0);
    }

}