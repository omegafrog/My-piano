package com.omegafrog.My.piano.app.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.parameters.P;

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
