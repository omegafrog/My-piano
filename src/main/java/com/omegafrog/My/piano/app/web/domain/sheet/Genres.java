package com.omegafrog.My.piano.app.web.domain.sheet;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.omegafrog.My.piano.app.web.enums.Genre;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Genres {
	private Genre genre1;
	private Genre genre2;

	@Builder
	public Genres(Genre genre1, Genre genre2) {
		this.genre1 = genre1;
		this.genre2 = genre2;
	}

	public List<Genre> genreLists() {
		List<Genre> result = new ArrayList<>();
		if (genre1 != null)
			result.add(genre1);
		if (genre2 != null)
			result.add(genre2);
		return result;
	}
}
