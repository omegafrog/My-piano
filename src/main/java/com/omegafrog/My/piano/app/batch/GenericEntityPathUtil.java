package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;

public class GenericEntityPathUtil {
    public static <T> NumberPath<Long> getLongProperty(EntityPathBase<T> entityPathBase, String propertyName) {
        PathBuilder<T> pathBuilder = new PathBuilder<>(entityPathBase.getType(), entityPathBase.getMetadata());
        return pathBuilder.getNumber(propertyName, Long.class);
    }

    public static <T> NumberPath<Integer> getIntegerProperty(EntityPathBase<T> entityPathBase, String propertyName) {
        PathBuilder<T> pathBuilder = new PathBuilder<>(entityPathBase.getType(), entityPathBase.getMetadata());
        return pathBuilder.getNumber(propertyName, Integer.class);
    }

    public static <T extends Article> Path<T> getProperty(EntityPathBase<T> entityPath, String propertyName, Class<T> propertyClass) {
        PathBuilder<T> pathBuilder = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
        return pathBuilder.get(propertyName, propertyClass);
    }
}
