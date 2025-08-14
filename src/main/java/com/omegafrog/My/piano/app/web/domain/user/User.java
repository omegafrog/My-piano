package com.omegafrog.My.piano.app.web.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.relation.FollowedUser;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedLesson;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedSheetPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserLikedVideoPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserPurchasedItem;
import com.omegafrog.My.piano.app.web.domain.relation.UserPurchasedLesson;
import com.omegafrog.My.piano.app.web.domain.relation.UserPurchasedSheetPost;
import com.omegafrog.My.piano.app.web.domain.relation.UserScrappedLesson;
import com.omegafrog.My.piano.app.web.domain.relation.UserScrappedSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.sheetPost.ArtistInfo;
import com.omegafrog.My.piano.app.web.dto.user.ChangeUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.exception.AlreadyLikedEntityException;
import com.omegafrog.My.piano.app.web.exception.AlreadyScrappedEntityException;
import com.omegafrog.My.piano.app.web.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.exception.payment.NotEnoughCashException;
import com.omegafrog.My.piano.app.web.exception.payment.PaymentException;
import com.omegafrog.My.piano.app.web.vo.user.AlarmProperties;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "person")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	private Long id;

	@NotBlank(message = "name cannot be null")
	private String name;

	@Email
	@NotBlank(message = "email cannot be null")
	@Column(unique = true)
	private String email;

	@NotNull
	private LoginMethod loginMethod;

	@Value("${user.baseProfileSrc}")
	private String profileSrc;
	@PositiveOrZero
	private int point;
	@PositiveOrZero
	private int cash;

	@Nullable
	private PhoneNum phoneNum;
	@NotNull
	private AlarmProperties alarmProperties;

	@JsonManagedReference(value = "user-securityUser")
	@OneToOne(mappedBy = "user")
	@Setter
	private SecurityUser securityUser;

	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	@NotNull
	private Cart cart;

	@JsonManagedReference
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "owner")
	private final List<Coupon> coupons = new ArrayList<>();

	@JsonManagedReference("purchased-sheet-user")
	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private final List<UserPurchasedSheetPost> purchasedSheets = new ArrayList<>();

	@JsonManagedReference("purchased-lesson-user")
	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private final List<UserPurchasedLesson> purchasedLessons = new ArrayList<>();

	@JsonManagedReference("scrapped-sheetpost-user")
	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private final List<UserScrappedSheetPost> scrappedSheetPosts = new ArrayList<>();

	@JsonManagedReference("user-scrapped-lesson")
	@OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private final List<UserScrappedLesson> scrappedLesson = new ArrayList<>();

	@JsonManagedReference
	@OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "author")
	private final List<SheetPost> uploadedSheetPosts = new ArrayList<>();

	@JsonManagedReference
	@OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "author")
	private final List<Lesson> uploadedLessons = new ArrayList<>();

	@JsonManagedReference
	@OneToMany(mappedBy = "author", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
	private final List<Post> uploadedPosts = new ArrayList<>();

	@JsonManagedReference
	@OneToMany(mappedBy = "author", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
	private final List<VideoPost> uploadedVideoPosts = new ArrayList<>();

	@JsonManagedReference("followed-user-me")
	@OneToMany(mappedBy = "follower", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private final List<FollowedUser> followed = new CopyOnWriteArrayList<>();

	@JsonManagedReference("liked-post-user")
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserLikedPost> likedPosts = new ArrayList<>();

	@JsonManagedReference("liked-sheetpost-user")
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserLikedSheetPost> likedSheetPosts = new ArrayList<>();

	@JsonManagedReference("liked-video-user")
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserLikedVideoPost> likedVideoPosts = new ArrayList<>();

	@JsonManagedReference("liked-lesson-user")
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserLikedLesson> likedLessons = new ArrayList<>();

	@JsonManagedReference("comment-user")
	@OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL)
	private final List<Comment> wroteComments = new ArrayList<>();

	public int chargeCash(int cash) {
		this.cash += cash;
		return this.cash;
	}

	public void addUploadedPost(Post post) {
		uploadedPosts.add(post);
		if (post.getAuthor() != this) {
			post.setAuthor(this);
		}
	}

	public void addUploadedLesson(Lesson lesson) {
		uploadedLessons.add(lesson);
		if (lesson.getAuthor() != this)
			lesson.setAuthor(this);
	}

	public void addUploadedVideoPost(VideoPost videoPost) {
		uploadedVideoPosts.add(videoPost);
		if (videoPost.getAuthor() != this) {
			videoPost.setAuthor(this);
		}
	}

	public boolean isScrappedLesson(Lesson lesson) {
		return scrappedLesson.stream().anyMatch(item -> item.getLesson().equals(lesson));
	}

	public void scrapLesson(Lesson lesson) {
		if (isScrappedLesson(lesson))
			throw new EntityExistsException("이미 스크랩한 레슨입니다. id:" + lesson.getId());
		scrappedLesson.add(
			UserScrappedLesson.builder()
				.lesson(lesson)
				.user(this)
				.build());
	}

	public void unScrapLesson(Lesson lesson) {
		if (!isScrappedLesson(lesson))
			throw new EntityNotFoundException("스크랩하지 않은 레슨을 취소하려고 합니다." + lesson.getId());
		scrappedLesson.removeIf(l -> l.getLesson().equals(lesson));
	}

	public void scrapSheetPost(SheetPost sheetPost) {
		if (isScrappedSheetPost(sheetPost))
			throw new AlreadyScrappedEntityException("이미 스크랩한 entity입니다.");
		scrappedSheetPosts.add(
			UserScrappedSheetPost.builder()
				.sheetPost(sheetPost)
				.user(this)
				.build());
	}

	public boolean isScrappedSheetPost(SheetPost sheetPost) {
		return scrappedSheetPosts.stream().anyMatch(item -> item.getSheetPost().equals(sheetPost));
	}

	// 좋아요 누른 sheetpost list에 추가한다.
	public void likeSheetPost(SheetPost sheetPost) {
		if (isLikedSheetPost(sheetPost))
			throw new AlreadyLikedEntityException();
		likedSheetPosts.add(UserLikedSheetPost.builder()
			.user(this)
			.sheetPost(sheetPost)
			.build());
	}

	private boolean isLikedSheetPost(SheetPost sheetPost) {
		return likedSheetPosts.stream().anyMatch(item -> item.getSheetPost().equals(sheetPost));
	}

	public void dislikeSheetPost(SheetPost sheetPost) {
		boolean removed = likedSheetPosts.removeIf(item -> item.getSheetPost().equals(sheetPost));
		if (!removed)
			throw new EntityNotFoundException("sheetPost entity를 찾을 수 없습니다.id:" + sheetPost.getId());
		sheetPost.decreaseLikedCount();
	}

	public void deleteUploadedPost(Post post) {
		uploadedPosts.remove(post);
	}

	public void likeVideoPost(VideoPost videoPost) {
		if (likedVideoPosts.stream().anyMatch(item -> item.getVideoPost().equals(videoPost)))
			throw new EntityExistsException(ExceptionMessage.ENTITY_EXISTS);
		likedVideoPosts.add(UserLikedVideoPost.builder()
			.videoPost(videoPost)
			.user(this)
			.build());
		videoPost.increaseLikedCount();
	}

	public void dislikeVideoPost(VideoPost videoPost) {
		if (!likedVideoPosts.removeIf(likedVideoPost -> likedVideoPost.getVideoPost().equals(videoPost)))
			throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST);
	}

	public void addWroteComments(Comment comment) {
		wroteComments.add(comment);
	}

	public void deleteWroteComments(Comment comment, User loggedInUser) {
		boolean isCommentRemoved = wroteComments.removeIf(
			element -> {
				if (element.equals(comment)) {
					if (element.getAuthor().equals(loggedInUser))
						return true;
					else
						throw new AccessDeniedException("Cannot delete other user's comment.");
				} else
					return false;
			}
		);
		if (!isCommentRemoved)
			throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT);
	}

	@Builder
	public User(String name, String email, Cart cart, LoginMethod loginMethod, String profileSrc,
		@Nullable PhoneNum phoneNum, int cash) {

		this.email = email;
		this.name = name;
		this.alarmProperties = new AlarmProperties();
		this.loginMethod = loginMethod;
		this.profileSrc = profileSrc;
		this.phoneNum = phoneNum;
		this.cart = cart;
		this.cash = cash;
	}

	public User update(ChangeUserDto userDto) {
		this.name = userDto.getName();
		this.profileSrc = userDto.getProfileSrc();
		this.phoneNum = new PhoneNum(userDto.getPhoneNum());
		return this;
	}

	public void likePost(Post post) {
		// already liked post check
		assert !likedPosts.stream().anyMatch(p -> p.getPost().equals(post));
		likedPosts.add(UserLikedPost.builder()
			.post(post)
			.user(this)
			.build());
	}

	public void dislikePost(Post dislikedPost) {
		likedPosts.removeIf(item -> item.getPost().equals(dislikedPost));
	}

	public void addCash(int cash) {
		this.cash += cash;
	}

	public void pay(Order order) throws PaymentException, ClassCastException {
		if (cash < order.getTotalPrice()) {
			throw new NotEnoughCashException("Cannot buy this item => cash:"
				+ cash + " < price:" + order.getTotalPrice());
			// 캐시 부족
		}

		cash -= order.getTotalPrice();
		order.setStatus(OrderStatus.IN_PROGRESS);
		if (order.getItem() instanceof Lesson item)
			purchasedLessons.add(
				UserPurchasedLesson.builder()
					.lesson(item)
					.user(this)
					.build()
			);
		else if (order.getItem() instanceof SheetPost item)
			purchasedSheets.add(
				UserPurchasedSheetPost.builder()
					.sheetPost(item)
					.user(this)
					.build()
			);
		else
			throw new ClassCastException("Cannot cast this class to child class.");
	}

	public void receiveCash(int totalPrice) {
		cash += totalPrice;
	}

	/**
	 * 유저 엔티티가 이미 구매한 상품인지 확인하는 함수
	 *
	 * @param item 구매 여부를 확인할 상품 엔티티. SellableItem의 서브클래스의 인스턴스.
	 * @return 구매한 상품이라면 true, 구매하지 않은 상품이라면 false
	 */
	public boolean isPurchased(SellableItem item) {
		List<? extends UserPurchasedItem> purchasedItemList;
		if (item instanceof SheetPost)
			purchasedItemList = purchasedSheets;
		else if (item instanceof Lesson)
			purchasedItemList = purchasedLessons;
		else
			throw new ClassCastException("Cannot find SellableItem collection");

		return purchasedItemList.stream().anyMatch(purchasedItem -> purchasedItem.getItem().equals(item));
	}

	public UserInfo getUserInfo() {
		return UserInfo.builder()
			.id(id)
			.name(name)
			.username(securityUser.getUsername())
			.email(email)
			.profileSrc(profileSrc)
			.loginMethod(loginMethod)
			.role(securityUser.getRole())
			.enabled(!securityUser.isLocked())
			.cash(cash)
			.build();
	}

	public ArtistInfo getArtistInfo() {
		return ArtistInfo.builder()
			.id(id)
			.name(name)
			.username(securityUser.getUsername())
			.email(email)
			.profileSrc(profileSrc)
			.build();
	}

	public UserProfileDto getUserProfileDto() {
		return new UserProfileDto(id, securityUser.getUsername(), profileSrc);
	}

	public void deleteUploadedVideoPost(VideoPost videoPost) {
		if (!uploadedVideoPosts.remove(videoPost))
			throw new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_VIDEO_POST);
	}

	public void likeLesson(Lesson lesson) {
		if (likedLessons.stream().anyMatch(item -> item.getLesson().equals(lesson)))
			throw new EntityExistsException("이미 좋아요를 누른 글입니다.");
		likedLessons.add(UserLikedLesson.builder()
			.lesson(lesson)
			.user(this)
			.build());
	}

	public boolean isLikedLesson(Lesson lesson) {
		return likedLessons.stream().anyMatch(item -> item.getLesson().equals(lesson));
	}

	public void dislikeLesson(Lesson lesson) {
		likedLessons.removeIf(item -> item.getLesson().equals(lesson));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User)o;
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void unScrapSheetPost(SheetPost sheetPost) {
		boolean removed = scrappedSheetPosts.removeIf(item -> item.getSheetPost().equals(sheetPost));
		if (!removed)
			throw new EntityNotFoundException("스크랩하지 않았습니다.");
	}

	//    public boolean deleteOrder(Order order) {
	//        SellableItem item = order.getItem();
	//        boolean removed = false;
	//        if (item instanceof SheetPost)
	//        else if (order.getItem() instanceof Lesson)
	//        else throw new ClassCastException("Wrong Order Item class.");
	//        return removed;
	//    }

	public boolean deletePurchasedSheetPost(Order order) {
		return purchasedSheets.removeIf(purchased -> purchased.getSheetPost().equals(order.getItem()));
	}

	public boolean deletePurchasedLesson(Order order) {
		return purchasedLessons.removeIf(purchasedLesson -> purchasedLesson.getLesson().equals(order.getItem()));
	}
}
