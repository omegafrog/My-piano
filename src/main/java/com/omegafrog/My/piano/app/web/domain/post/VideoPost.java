package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class VideoPost extends Article {

    @NotEmpty
    private String videoUrl;

    @Override
    public void setAuthor(User author){
        this.author = author;
        if(!author.getUploadedVideoPosts().contains(this)){
            author.addUploadedVideoPost(this);
        }
    }

    @Builder
    public VideoPost(User author, String title, String content, String videoUrl) {
        this.author=author;
        this.title=title;
        this.content = content;
        this.videoUrl = videoUrl;
    }

    /**
     * VideoPost의 내용을 수정한다.
     * @param updateVideoPostDto update할 내용이 담긴 DTO
     * @return this : 수정된 VideoPost자신을 반환한다.
     */
    public void update(UpdateVideoPostDto updateVideoPostDto){
         this.viewCount=updateVideoPostDto.getViewCount();
         this.title= updateVideoPostDto.getTitle();
         this.content= updateVideoPostDto.getContent();
         this.videoUrl= updateVideoPostDto.getVideoUrl();
    }

    public VideoPostDto toDto(){
        return VideoPostDto.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author.getUserProfile())
                .likeCount(likeCount)
                .viewCount(viewCount)
                .createdAt(createdAt)
                .comments(getComments().stream().map(Comment::toDto).toList())
                .videoUrl(videoUrl)
                .build();
    }
}
