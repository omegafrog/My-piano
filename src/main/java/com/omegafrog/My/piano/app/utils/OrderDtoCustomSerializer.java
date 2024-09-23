package com.omegafrog.My.piano.app.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;

import java.io.IOException;

public class OrderDtoCustomSerializer extends JsonSerializer<OrderDto> {
    @Override
    public void serialize(OrderDto orderDto, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("id");
        jsonGenerator.writeString(String.valueOf(orderDto.getId()));
        jsonGenerator.writeFieldName("item");
        if (orderDto.getItem() instanceof SheetPostDto) {
            jsonGenerator.writeObject(
                    (SheetPostDto) orderDto.getItem()
            );
        } else {
            jsonGenerator.writeObject(
                    (LessonDto) orderDto.getItem()
            );
        }
        jsonGenerator.writeObjectField("seller", orderDto.getSeller());
        jsonGenerator.writeObjectField("buyer", orderDto.getBuyer());
        jsonGenerator.writeObjectField("initialPrice", orderDto.getInitialPrice());
        jsonGenerator.writeObjectField("totalPrice", orderDto.getTotalPrice());
        jsonGenerator.writeObjectField("discountRate", orderDto.getDiscountRate());
        jsonGenerator.writeObjectField("coupon", orderDto.getCoupon());
        jsonGenerator.writeEndObject();
    }
}
