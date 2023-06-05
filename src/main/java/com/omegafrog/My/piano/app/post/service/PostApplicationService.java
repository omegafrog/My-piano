package com.omegafrog.My.piano.app.post.service;

import com.omegafrog.My.piano.app.post.entity.Post;
import com.omegafrog.My.piano.app.post.entity.PostRepository;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.entity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    public void likePost(User user, Post post){

        user = user.addLikedPost(post);
        userRepository.save(user);
        postRepository.save(post);
    }
}
