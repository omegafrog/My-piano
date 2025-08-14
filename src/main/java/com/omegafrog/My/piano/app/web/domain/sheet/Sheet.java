package com.omegafrog.My.piano.app.web.domain.sheet;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
public class Sheet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private int pageNum;
	private Difficulty difficulty;
	private Instrument instrument;
	@Embedded
	private Genres genres;
	private boolean isSolo;
	private boolean lyrics;
	private String sheetUrl;
	@Lob
	private String thumbnailUrl;
	private String originalFileName;

	@JsonBackReference("sheetpost")
	@OneToOne(mappedBy = "sheet")
	@Setter
	private SheetPost sheetPost;

	private LocalDateTime createdAt = LocalDateTime.now();

	@JsonBackReference
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name = "CREATOR_ID")
	private User user;

	@Builder
	public Sheet(String title,
		int pageNum,
		Difficulty difficulty,
		Instrument instrument,
		Genres genres,
		boolean isSolo,
		boolean lyrics,
		String sheetUrl, String thumbnailUrl,
		User user,
		String originalFileName,
		SheetPost sheetPost) {
		this.title = title;
		this.pageNum = pageNum;
		this.difficulty = difficulty;
		this.instrument = instrument;
		this.genres = genres;
		this.isSolo = isSolo;
		this.lyrics = lyrics;
		this.sheetUrl = sheetUrl;
		this.thumbnailUrl = thumbnailUrl;
		this.user = user;
		this.originalFileName = originalFileName;
		this.sheetPost = sheetPost;
	}

	public Sheet update(UpdateSheetDto dto) {
		this.title = dto.getTitle();
		this.pageNum = dto.getPageNum();
		this.difficulty = dto.getDifficulty();
		this.instrument = dto.getInstrument();
		this.genres = dto.getGenres();
		this.isSolo = dto.isSolo();
		this.lyrics = dto.isLyrics();
		this.sheetUrl = dto.getSheetUrl();
		this.originalFileName = dto.getOriginalFileName();
		return this;
	}

	public void updateUrls(String sheetUrl, String thumbnailUrl) {
		this.sheetUrl = sheetUrl;
		this.thumbnailUrl = thumbnailUrl;
	}

	public void updatePageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public void updateOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public SheetDto toSheetDto() {
		return SheetDto.builder()
			.id(id)
			.createdAt(createdAt)
			.difficulty(difficulty)
			.genres(genres)
			.sheetUrl(sheetUrl)
			.thumbnailUrl(thumbnailUrl)
			.originalFileName(originalFileName)
			.instrument(instrument)
			.lyrics(lyrics)
			.pageNum(pageNum)
			.isSolo(isSolo)
			.title(title)
			.user(user.getUserInfo())
			.originalFileName(originalFileName)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Sheet sheet = (Sheet)o;

		return id.equals(sheet.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}


