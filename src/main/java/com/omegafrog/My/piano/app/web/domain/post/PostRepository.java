package com.omegafrog.My.piano.app.web.domain.post;


import java.util.Optional;

public interface PostRepository  {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void deleteById(Long id);

    void deleteAll();

}