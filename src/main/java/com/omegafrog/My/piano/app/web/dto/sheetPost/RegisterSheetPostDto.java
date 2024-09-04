package com.omegafrog.My.piano.app.web.dto.sheetPost;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.validator.constraints.Range;

@Data
@Setter
@Getter
@NoArgsConstructor
public class RegisterSheetPostDto {
    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    @PositiveOrZero
    private int price;
    @Range(min = 0, max = 1L)
    private Double discountRate;
    @NotNull
    private RegisterSheetDto sheet;

    @Builder
    public RegisterSheetPostDto(String title, String content, int price, Double discountRate, RegisterSheetDto sheetDto) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.discountRate = discountRate;
        this.sheet = sheetDto;
    }
}
