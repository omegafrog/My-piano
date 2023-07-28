package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import lombok.*;


@Data
@NoArgsConstructor
@Setter
@Getter
public class UpdateSheetPostDto {
    private String title;
    private String content;
    private UpdateSheetDto sheetDto;
    private Integer price;
    private Double discountRate;

    @Builder
    public UpdateSheetPostDto(String title, String content, UpdateSheetDto sheetDto, int price, Double discountRate) {
        this.title = title;
        this.content = content;
        this.sheetDto = sheetDto;
        this.price = price;
        this.discountRate = discountRate;
    }

}
