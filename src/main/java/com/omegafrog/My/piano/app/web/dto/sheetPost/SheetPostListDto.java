package com.omegafrog.My.piano.app.web.dto.sheetPost;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;

import lombok.Getter;

@Getter
public class SheetPostListDto implements Serializable {
	private Long id;
	private String title;
	private String artistName;
	private String artistProfile;
	private String sheetTitle;
	private Difficulty difficulty;
	private Genres genres;
	private Instrument instrument;
	private LocalDateTime createdAt;
	private Integer price;
	private Integer viewCount;

	protected SheetPostListDto() {
	}

	public SheetPostListDto(Long id, String title, String artistName, String artistProfile, String sheetTitle,
		Difficulty difficulty, Genres genres, Instrument instrument, LocalDateTime createdAt, Integer price) {
		this.id = id;
		this.title = title;
		this.artistName = artistName;
		this.artistProfile = artistProfile;
		this.sheetTitle = sheetTitle;
		this.difficulty = difficulty;
		this.genres = genres;
		this.instrument = instrument;
		this.createdAt = createdAt;
		this.price = price;
	}

	public void updateViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	@Override
	public String toString() {
		return "SheetPostListDto [id=" + id + ", title=" + title + ", artistName=" + artistName + ", artistProfile="
			+ artistProfile + ", sheetTitle=" + sheetTitle + ", difficulty=" + difficulty + ", genres=" + genres
			+ ", instrument=" + instrument + ", createdAt=" + createdAt + ", price=" + price + ", viewCount="
			+ viewCount + "]";
	}
}
