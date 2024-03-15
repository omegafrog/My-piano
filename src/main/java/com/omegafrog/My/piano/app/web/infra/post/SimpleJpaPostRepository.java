package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimpleJpaPostRepository extends JpaRepository<Post, Long> {
    @Query(nativeQuery = true, value = "delete from liked_post where post_id=:id")
    @Modifying
    int deleteAllLikedPostById(@Param("id") Long id);
}
