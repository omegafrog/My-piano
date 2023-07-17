package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import lombok.Builder;
import lombok.Data;


@Data
public class UpdateSheetPostDto {
    private String title;
    private String content;
    private Sheet sheet;
    private int price;
    private Long discountRate;

    @Builder
    public UpdateSheetPostDto(String title, String content, Sheet sheet, int price, Long discountRate) {
        this.title = title;
        this.content = content;
        this.sheet = sheet;
        this.price = price;
        this.discountRate = discountRate;
    }

}
