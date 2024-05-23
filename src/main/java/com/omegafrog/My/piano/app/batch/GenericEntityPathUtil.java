package com.omegafrog.My.piano.app.batch;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;

public class GenericEntityPathUtil {
    public static <T> NumberPath<Long> getLongProperty(EntityPathBase<T> entityPathBase, String propertyName){
        PathBuilder<T> pathBuilder = new PathBuilder<>(entityPathBase.getType(), entityPathBase.getMetadata());
        return pathBuilder.getNumber(propertyName, Long.class);
    }
}
