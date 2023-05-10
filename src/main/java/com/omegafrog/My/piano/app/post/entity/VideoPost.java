package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.dto.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@NoArgsConstructor
@Getter
public class VideoPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "USER_ID")
    private User author;

    private LocalDateTime createdAt;

    private int viewCount;

    private String title;

    private String content;

    private String videoUrl;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    private final List<Comment> comments = new CopyOnWriteArrayList<>();

    @Builder
    public VideoPost(User author, String title, String content, String videoUrl) {
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
    }

    /**
     * VideoPost의 내용을 수정한다.
     * @param updateVideoPostDto update할 내용이 담긴 DTO
     * @return this : 수정된 VideoPost자신을 반환한다.
     */
    public VideoPost update(UpdateVideoPostDto updateVideoPostDto){
         this.viewCount=updateVideoPostDto.getViewCount();
         this.title= updateVideoPostDto.getTitle();
         this.content= updateVideoPostDto.getContent();
         this.videoUrl= updateVideoPostDto.getVideoUrl();
         return this;
    }

    /**
     * 댓글을 추가한다.
     * @param comment  추가할 comment entity
     * @return comments의 길이를 반환한다.
     */
    public int addComment(Comment comment){
        this.comments.add(comment);
        return this.comments.size();
    }

    /**
     * id를 가진 댓글을 삭제한다.
     * @param id 삭제할 댓글의 id
     */
    public void deleteComment(Long id){
        this.comments.forEach(comment -> {
            if (comment.getId().equals(id)) comments.remove(comment);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoPost videoPost = (VideoPost) o;

        return id.equals(videoPost.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
