package com.omegafrog.My.piano.app.web.domain.post;


import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository  {

    Post save(Post post);

    Post saveAndFlush(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);

    void deleteAll();

    List<Post> findAll(Pageable pageable, Sort sort);
    Page<Post> findAll(SearchPostFilter filter, Pageable pageable);

    Long count();
}
