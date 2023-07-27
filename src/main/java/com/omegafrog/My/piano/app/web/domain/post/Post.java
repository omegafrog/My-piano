package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Post extends Article {


    public Post update(UpdatePostDto post){
        super.viewCount = post.getViewCount();
        super.title = post.getTitle();
        super.content = post.getContent();
        super.likeCount = post.getLikeCount();
        return this;
    }

    @Builder
    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }



    public PostDto toDto(){
        return PostDto.builder()
                .id(id)
                .createdAt(createdAt)
                .title(title)
                .content(content)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .comments(comments)
                .author(author.getUserProfile())
                .build();
    }


}

