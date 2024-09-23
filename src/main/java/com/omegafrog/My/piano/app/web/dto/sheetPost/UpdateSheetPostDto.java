package com.omegafrog.My.piano.app.web.dto.sheetPost;

import lombok.*;


@Data
@NoArgsConstructor
@Setter
@Getter
@ToString
public class UpdateSheetPostDto {
    private String title;
    private String content;
    private UpdateSheetDto sheet;
    private Integer price;
    private Double discountRate;

    @Builder
    public UpdateSheetPostDto(String title, String content, UpdateSheetDto sheet, int price, Double discountRate) {
        this.title = title;
        this.content = content;
        this.sheet = sheet;
        this.price = price;
        this.discountRate = discountRate;
    }

}
