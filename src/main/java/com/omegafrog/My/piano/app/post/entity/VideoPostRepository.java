package com.omegafrog.My.piano.app.post.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface VideoPostRepository extends JpaRepository<VideoPost, Long> {

    VideoPost save(VideoPost videoPost);

    Optional<VideoPost> findById(Long id);

    void deleteById(Long id);

}
