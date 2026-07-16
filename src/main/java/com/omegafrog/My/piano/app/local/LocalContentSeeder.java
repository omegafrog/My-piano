package com.omegafrog.My.piano.app.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.enums.PostType;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LocalContentSeeder {

    static final String SENTINEL_EMAIL = "local.library@mypiano.dev";
    private static final String PUBLIC_BASE_URL = "http://localhost:8080";
    private static final String CONTENT_RESOURCE = "local-seed/content.json";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SheetPostRepository sheetPostRepository;
    private final CommentRepository commentRepository;
    private final ObjectMapper objectMapper;
    private final Path storageBasePath;

    public LocalContentSeeder(
            UserRepository userRepository,
            PostRepository postRepository,
            SheetPostRepository sheetPostRepository,
            CommentRepository commentRepository,
            ObjectMapper objectMapper,
            String storageBasePath) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.sheetPostRepository = sheetPostRepository;
        this.commentRepository = commentRepository;
        this.objectMapper = objectMapper;
        this.storageBasePath = Path.of(storageBasePath).toAbsolutePath().normalize();
    }

    @Transactional
    public void seed() throws IOException {
        if (userRepository.findByEmail(SENTINEL_EMAIL).isPresent()) {
            return;
        }

        SeedContent content = readContent();
        copyAssets(content);
        List<User> users = saveUsers(content.users());
        savePosts(content.posts(), users);
        saveSheetPosts(content.sheetPosts(), users);
    }

    private SeedContent readContent() throws IOException {
        try (InputStream input = new ClassPathResource(CONTENT_RESOURCE).getInputStream()) {
            return objectMapper.readValue(input, SeedContent.class);
        }
    }

    private void copyAssets(SeedContent content) throws IOException {
        for (UserSeed user : content.users()) {
            copyAsset("profiles", user.profileAsset(), user.profileFile());
        }
        for (SheetPostSeed sheetPost : content.sheetPosts()) {
            copyAsset("thumbnails", sheetPost.thumbnailAsset(), sheetPost.thumbnailFile());
            copyBase64Asset("sheets", sheetPost.sheetAsset(), sheetPost.sheetFile());
        }
    }

    private void copyAsset(String directory, String sourceName, String targetName) throws IOException {
        Path target = targetPath(directory, targetName);
        Files.createDirectories(target.getParent());
        try (InputStream input = new ClassPathResource(
                "local-seed/assets/" + directory + "/" + sourceName).getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void copyBase64Asset(String directory, String sourceName, String targetName) throws IOException {
        Path target = targetPath(directory, targetName);
        Files.createDirectories(target.getParent());
        try (InputStream input = new ClassPathResource(
                "local-seed/assets/" + directory + "/" + sourceName).getInputStream()) {
            String encoded = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
            Files.write(target, Base64.getMimeDecoder().decode(encoded));
        }
    }

    private Path targetPath(String directory, String targetName) {
        Path target = storageBasePath.resolve(directory).resolve(targetName).normalize();
        if (!target.startsWith(storageBasePath)) {
            throw new IllegalArgumentException("Asset target must stay inside local storage");
        }
        return target;
    }

    private List<User> saveUsers(List<UserSeed> seeds) {
        List<User> users = new ArrayList<>();
        for (UserSeed seed : seeds) {
            User user = User.builder()
                    .name(seed.name())
                    .email(seed.email())
                    .loginMethod(LoginMethod.EMAIL)
                    .profileSrc(publicUrl("profiles", seed.profileFile()))
                    .cart(new Cart())
                    .cash(10_000)
                    .build();
            user.setSecurityUser(SecurityUser.builder()
                    .username(seed.username())
                    .password("local-content-password")
                    .role(Role.valueOf(seed.role()))
                    .user(user)
                    .build());
            users.add(userRepository.save(user));
        }
        return users;
    }

    private void savePosts(List<PostSeed> seeds, List<User> users) {
        for (PostSeed seed : seeds) {
            User author = users.get(seed.author());
            Post post = Post.builder()
                    .author(author)
                    .title(seed.title())
                    .content(seed.content())
                    .type(PostType.COMMON)
                    .build();
            author.addUploadedPost(post);
            Comment comment = addComment(post, users.get(seed.commentAuthor()), seed.comment());
            postRepository.save(post);
            commentRepository.save(comment);
        }
    }

    private void saveSheetPosts(List<SheetPostSeed> seeds, List<User> users) {
        for (SheetPostSeed seed : seeds) {
            User artist = users.get(seed.author());
            Sheet sheet = Sheet.builder()
                    .title(seed.sheetTitle())
                    .pageNum(1)
                    .difficulty(Difficulty.valueOf(seed.difficulty()))
                    .instrument(Instrument.valueOf(seed.instrument()))
                    .genres(Genres.builder()
                            .genre1(Genre.valueOf(seed.genrePrimary()))
                            .genre2(Genre.valueOf(seed.genreSecondary()))
                            .build())
                    .isSolo(seed.solo())
                    .lyrics(seed.lyrics())
                    .sheetUrl(publicUrl("sheets", seed.sheetFile()))
                    .thumbnailUrl(publicUrl("thumbnails", seed.thumbnailFile()))
                    .originalFileName(seed.originalFileName())
                    .user(artist)
                    .build();
            SheetPost sheetPost = SheetPost.builder()
                    .title(seed.title())
                    .content(seed.content())
                    .artist(artist)
                    .sheet(sheet)
                    .price(seed.price())
                    .build();
            sheet.setSheetPost(sheetPost);
            Comment comment = addComment(sheetPost, users.get(seed.commentAuthor()), seed.comment());
            sheetPostRepository.save(sheetPost);
            commentRepository.save(comment);
        }
    }

    private Comment addComment(Article article, User author, String content) {
        Comment comment = Comment.builder()
                .author(author)
                .content(content)
                .build();
        article.addComment(comment);
        return comment;
    }

    private String publicUrl(String directory, String fileName) {
        return PUBLIC_BASE_URL + "/" + directory + "/" + fileName;
    }

    record SeedContent(List<UserSeed> users, List<PostSeed> posts, List<SheetPostSeed> sheetPosts) {
    }

    record UserSeed(
            String name,
            String username,
            String email,
            String role,
            String profileAsset,
            String profileFile) {
    }

    record PostSeed(
            String title,
            String content,
            int author,
            String comment,
            int commentAuthor) {
    }

    record SheetPostSeed(
            String title,
            String content,
            String sheetTitle,
            int author,
            int commentAuthor,
            String comment,
            int price,
            String difficulty,
            String instrument,
            String genrePrimary,
            String genreSecondary,
            boolean solo,
            boolean lyrics,
            String originalFileName,
            String sheetAsset,
            String sheetFile,
            String thumbnailAsset,
            String thumbnailFile) {
    }
}
