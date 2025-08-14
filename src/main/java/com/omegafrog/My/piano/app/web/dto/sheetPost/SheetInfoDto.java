package com.omegafrog.My.piano.app.web.dto.sheetPost;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetInfoDto {
	private Long id;
	private String title;
	private String content;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
	private ArtistInfo artist;
	private Genres genres;
	private int pageNum;
	private Difficulty difficulty;
	private Instrument instrument;
	private boolean isSolo;
	private boolean lyrics;
	private String sheetUrl;
	private String thumbnailUrl;
	private String originalFileName;

	@Builder
	public SheetInfoDto(Long id, String title, String content, LocalDateTime createdAt, ArtistInfo artist,
		Genres genres, int pageNum, Difficulty difficulty, Instrument instrument, boolean isSolo,
		boolean lyrics, String sheetUrl, String originalFileName, String thumbnailUrl) {
		this.id = id;
		this.title = title;
		this.createdAt = createdAt;
		this.artist = artist;
		this.genres = genres;
		this.pageNum = pageNum;
		this.difficulty = difficulty;
		this.instrument = instrument;
		this.isSolo = isSolo;
		this.lyrics = lyrics;
		this.sheetUrl = sheetUrl;
		this.content = content;
		this.originalFileName = originalFileName;
		this.thumbnailUrl = thumbnailUrl;
	}
}
