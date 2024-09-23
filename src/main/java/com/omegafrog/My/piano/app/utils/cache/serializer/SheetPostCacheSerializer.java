package com.omegafrog.My.piano.app.utils.cache.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@RequiredArgsConstructor
public class SheetPostCacheSerializer implements RedisSerializer<SheetPost> {
    private final ObjectMapper objectMapper;

    @Override
    public byte[] serialize(SheetPost value) throws SerializationException {
        return new byte[0];
    }

    @Override
    public SheetPost deserialize(byte[] bytes) throws SerializationException {
        return null;
    }


}
