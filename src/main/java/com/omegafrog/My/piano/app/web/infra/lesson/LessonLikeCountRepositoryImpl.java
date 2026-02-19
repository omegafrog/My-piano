package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
@RequiredArgsConstructor
@Qualifier("LessonLikeCountRepositoryImpl")
public class LessonLikeCountRepositoryImpl implements LikeCountRepository {

    private final RedisTemplate<String, LessonLikeCount> redisTemplate;
    private final RedisLessonLikeCountRepository jpaRepository;
    private final LessonRepositoryImpl lessonRepository;

    @Override
    public int incrementLikeCount(Article article) {
        if (!exist(article.getId())) {
            LessonLikeCount saved = (LessonLikeCount) save(LessonLikeCount.builder()
                    .id(article.getId())
                    .likeCount(article.getLikeCount() + 1).build());
            return saved.getLikeCount();
        }
        return redisTemplate.opsForHash().increment(LessonLikeCount.KEY_NAME + ":" + article.getId(),
                "likeCount", 1L).intValue();
    }

    @Override
    public int decrementLikeCount(Article article) {
        redisTemplate.opsForHash().increment(LessonLikeCount.KEY_NAME + ":" + article.getId(),
                "likeCount", -1L);
        return article.getLikeCount() - 1;
    }

    @Override
    public LikeCount save(LikeCount likeCount) {
        redisTemplate.opsForHash().put(LessonLikeCount.KEY_NAME + ":" + likeCount.getId(),
                "likeCount", String.valueOf(likeCount.getLikeCount()));
        return likeCount;
    }

    @Override
    public LikeCount findById(Long articleId) {
        // redis에 존재하지 않으면 db에서 가져와 저장
        if (!exist(articleId)) {
            Lesson lesson = lessonRepository.findById(articleId)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post."));
            return save(new SheetPostLikeCount(articleId, lesson.getLikeCount()));
        }
        // redis에 존재할 경우 가져와서 반환
        return jpaRepository.findById(articleId).
                orElseThrow(() -> new EntityNotFoundException("Cannot find sheet like count."));
    }

    @Override
    public boolean exist(Long articleId) {
        return redisTemplate.opsForHash().hasKey(LessonLikeCount.KEY_NAME + ":" + articleId, "likeCount");
    }
}
