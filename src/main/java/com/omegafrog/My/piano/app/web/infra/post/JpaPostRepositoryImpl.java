package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPostRepositoryImpl implements PostRepository {

    private final SimpleJpaPostRepository postRepository;
    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Post> byId = findById(id);
        byId.get().getAuthor().deleteUploadedPost(byId.get());
        postRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        postRepository.deleteAll();
    }

    @Override
    public List<Post> findAll(Pageable pageable, Sort sort) {
        return postRepository.findAll(pageable).stream().sorted((o1, o2) -> {
            if (o1.getCreatedAt().isAfter(o2.getCreatedAt()))
                return -1;
            else if (o1.getCreatedAt().isBefore(o2.getCreatedAt()))
                return 1;
            else
                return 0;
        }).toList();
    }

    @Override
    public Long count() {
        return postRepository.count();
    }
}
