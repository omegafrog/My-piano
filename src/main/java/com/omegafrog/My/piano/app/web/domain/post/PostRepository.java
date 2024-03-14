package com.omegafrog.My.piano.app.web.domain.post;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository  {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);

    void deleteAll();

    List<Post> findAll(Pageable pageable, Sort sort);

    Long count();
}