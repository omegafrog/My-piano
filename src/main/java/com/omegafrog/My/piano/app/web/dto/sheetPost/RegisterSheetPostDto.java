package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.dto.RegisterSheetDto;
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
    @Range(min=0, max=1L)
    private Double discountRate;
    @NotNull
    private Long artistId;
    @NotNull
    private RegisterSheetDto sheet;

    @Builder
    public RegisterSheetPostDto(String title, String content, int price, Double discountRate, Long artistId, RegisterSheetDto sheetDto) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.discountRate = discountRate;
        this.artistId = artistId;
        this.sheet= sheetDto;
    }
}
