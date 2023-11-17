package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.RegisterSheetDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class RegisterSheetPostDto {
    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    private int price;
    private Double discountRate;
    @NotNull
    private Long artistId;
    @NotNull
    private RegisterSheetDto sheetDto;

    @Builder
    public RegisterSheetPostDto(String title, String content, int price, Double discountRate, Long artistId, RegisterSheetDto sheetDto) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.discountRate = discountRate;
        this.artistId = artistId;
        this.sheetDto = sheetDto;
    }
}
