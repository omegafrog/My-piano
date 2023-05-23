package com.omegafrog.My.piano.app.post.infrastructure;

import com.omegafrog.My.piano.app.post.entity.Post;
import com.omegafrog.My.piano.app.post.entity.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPostRepository implements PostRepository {

    private final SimpleJpaPostRepository jpaRepository;

    @Override
    public Post save(Post post) {
        return jpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
