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
    private String uploadId; // 새로운 파일 업로드를 위한 uploadId

    @Builder
    public UpdateSheetPostDto(String title, String content, UpdateSheetDto sheet, int price, Double discountRate, String uploadId) {
        this.title = title;
        this.content = content;
        this.sheet = sheet;
        this.price = price;
        this.discountRate = discountRate;
        this.uploadId = uploadId;
    }

}
