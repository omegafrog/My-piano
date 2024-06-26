package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.enums.PostType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Post extends Article {

    private PostType type;
    public void setAuthor(User author){
        this.author = author;
        if(!author.getUploadedPosts().contains(this)){
            author.addUploadedPost(this);
        }
    }
    public Post update(UpdatePostDto post){
        super.title = post.getTitle();
        super.content = post.getContent();
        return this;
    }

    @Builder
    public Post(User author, String title, String content, PostType type) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.type = type;
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
                .author(author.getUserProfileDto())
                .build();
    }
}

