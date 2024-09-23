package com.omegafrog.My.piano.app.web.dto.sheetPost;

import lombok.*;


@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistInfo {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String profileSrc;
}
