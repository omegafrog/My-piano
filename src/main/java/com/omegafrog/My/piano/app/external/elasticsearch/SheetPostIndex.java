package com.omegafrog.My.piano.app.external.elasticsearch;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.enums.Genre;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@Document(indexName = "sheetpost", writeTypeHint = WriteTypeHint.FALSE)
public class SheetPostIndex {

        @Id
        @Field(type = FieldType.Keyword)
        private Long id;
        @Field(type = FieldType.Text)
        private String name;
        @Field(type = FieldType.Text)
        private String title;
        @Field(type = FieldType.Keyword)
        private List<String> genre;
        @Field(type = FieldType.Keyword)
        private String instrument;
        @Field(type = FieldType.Keyword)
        private String difficulty;
        @Field(type = FieldType.Long)
        private Long creator;
        @Field(type=FieldType.Integer)
        private Integer viewCount;
        @Field(type = FieldType.Text)
        private String _class;
        @Field(type=FieldType.Date)
        private String created_at;

        public static SheetPostIndex of(SheetPost sheetPost){
                return SheetPostIndex.builder()
                        .id(sheetPost.getId())
                        .name(sheetPost.getSheet().getTitle())
                        .title(sheetPost.getTitle())
                        .genre(sheetPost.getSheet().getGenres().getAll().stream().map(Genre::name).toList())
                        .instrument(sheetPost.getSheet().getInstrument().name())
                        .difficulty(sheetPost.getSheet().getDifficulty().name())
                        .creator(sheetPost.getSheet().getUser().getId())
                        .created_at(sheetPost.getCreatedAt().toString())
                        .viewCount(sheetPost.getViewCount())
                        .build();

        }

}
