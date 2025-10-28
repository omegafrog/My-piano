package com.omegafrog.My.piano.app.web.domain.sheet;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedSheetPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserPurchasedSheetPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserScrappedSheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
public class SheetPost extends SellableItem {

	@JsonManagedReference("sheetpost")
	@OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "SHEET_ID")
	private Sheet sheet;

	@JsonManagedReference(("liked-sheetpost-sheetpost"))
	@OneToMany(mappedBy = "sheetPost", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserLikedSheetPost> likedUsers = new ArrayList<>();

	@JsonManagedReference("user-scrapped-sheetpost")
	@OneToMany(mappedBy = "sheetPost", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserScrappedSheetPost> scrappedUsers = new ArrayList<>();
	@JsonManagedReference
	@OneToMany(mappedBy = "sheetPost", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserPurchasedSheetPost> purchasedUsers = new ArrayList<>();

	@Builder
	public SheetPost(String title, String content, User artist, Sheet sheet, int price) {
		super(artist, title, content, price);
		this.title = title;
		this.content = content;
		this.sheet = sheet;
	}

	public SheetPost update(UpdateSheetPostDto dto) {
		super.updatePrice(dto.getPrice() == null ? price : dto.getPrice());
		super.updateDiscountRate(dto.getDiscountRate() == null ? discountRate : dto.getDiscountRate());
		this.title = dto.getTitle() == null ? title : dto.getTitle();
		if (dto.getSheet() != null) {
			UpdateSheetDto sheetDto = dto.getSheet();
			this.sheet = Sheet.builder()
					.title(sheetDto.getTitle())
					.genres(sheetDto.getGenres())
					.pageNum(sheetDto.getPageNum())
					.difficulty(sheetDto.getDifficulty())
					.instrument(sheetDto.getInstrument())
					.isSolo(sheetDto.getSolo())
					.lyrics(sheetDto.getLyrics())
					.user(author)
					.originalFileName(sheetDto.getOriginalFileName())
					.sheetPost(this)
					.sheetUrl(sheetDto.getSheetUrl())
					.thumbnailUrl(sheetDto.getThumbnailUrl())
					.build();
		}
		this.content = dto.getContent() == null ? content : dto.getContent();
		return this;
	}

	public SheetPostDto toDto() {
		return SheetPostDto.builder()
				.id(id)
				.title(title)
				.content(content)
				.likeCount(likeCount)
				.viewCount(viewCount)
				.discountRate(getDiscountRate())
				.comments(new ArrayList<>())
				.artist(author.getArtistInfo())
				.createdAt(createdAt)
				.sheet(toInfoDto())
				.price(getPrice())
				.disabled(disabled)
				.build();

	}

	public SheetInfoDto toInfoDto() {
		return SheetInfoDto.builder()
				.id(id)
				.title(sheet.getTitle())
				.content(content)
				.genres(sheet.getGenres())
				.instrument(sheet.getInstrument())
				.isSolo(sheet.isSolo())
				.lyrics(sheet.isLyrics())
				.difficulty(sheet.getDifficulty())
				.sheetUrl(sheet.getSheetUrl())
				.thumbnailUrl(sheet.getThumbnailUrl() == null ? "" : sheet.getThumbnailUrl())
				.artist(author.getArtistInfo())
				.pageNum(sheet.getPageNum())
				.originalFileName(sheet.getOriginalFileName())
				.createdAt(createdAt)
				.build();
	}

	public void updateSheet(Sheet sheet) {
		this.sheet = sheet;
	}
}