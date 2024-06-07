package com.omegafrog.My.piano.app.web.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserDto {
    @NotEmpty
    @Size(min = 2)
    private String name;

    private String currentPassword;

    private String changedPassword;

    private String profileSrc;

    private String phoneNum;


}
