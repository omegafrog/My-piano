package com.omegafrog.My.piano.app.external.elasticsearch;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.enums.Genre;
import lombok.*;

import java.lang.reflect.Array;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SheetPostIndex {
        private String name;
        private String title;
        private List<String> genre;
        private String instrument;
        private String difficulty;

        public static SheetPostIndex of(SheetPost sheetPost){
                return SheetPostIndex.builder()
                        .name(sheetPost.getSheet().getTitle())
                        .title(sheetPost.getTitle())
                        .genre( sheetPost.getSheet().getGenres().getAll().stream().map(Genre::name).toList())
                        .instrument(sheetPost.getSheet().getInstrument().name())
                        .difficulty(sheetPost.getSheet().getDifficulty().name())
                        .build();

        }
}
