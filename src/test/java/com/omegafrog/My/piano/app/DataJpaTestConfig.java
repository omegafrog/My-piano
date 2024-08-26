package com.omegafrog.My.piano.app;

import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.security.infrastructure.redis.RedisRefreshTokenRepository;
import com.omegafrog.My.piano.app.web.infra.Subscription.RedisSubscriptionRepository;
import com.omegafrog.My.piano.app.web.infra.lesson.RedisLessonViewCountRepository;
import com.omegafrog.My.piano.app.web.infra.post.RedisPostViewCountRepository;
import com.omegafrog.My.piano.app.web.infra.sheetPost.RedisSheetPostViewCountRepository;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@Import({QueryDslConfig.class, RedisConfig.class})
@ComponentScan(basePackages = {"com.omegafrog.My.piano.app.security.infrastructure", "com.omegafrog.My.piano.app.web.infra"})
@EnableJpaRepositories(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SheetPostIndexRepository.class,
                RedisLessonViewCountRepository.class,
                RedisRefreshTokenRepository.class,
                RedisPostViewCountRepository.class,
                RedisSheetPostViewCountRepository.class,
                RedisSubscriptionRepository.class
                }
))
@EnableRedisRepositories
public class DataJpaTestConfig {

    @Bean
    public Cleanup cleanup(){
        return new Cleanup();
    }
}
