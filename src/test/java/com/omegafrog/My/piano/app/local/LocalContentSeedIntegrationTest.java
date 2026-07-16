package com.omegafrog.My.piano.app.local;

import static org.assertj.core.api.Assertions.assertThat;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles({"test", "local-seed"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LocalContentSeedIntegrationTest {

    private static final Pattern FORBIDDEN_MARKER = Pattern.compile(
            "dummy|test|sample|fixture", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIMPLE_SEQUENCE = Pattern.compile(".*(?:[_ -]\\d+)(?:\\.[^.]+)?$");
    private static final Path STORAGE_ROOT = createStorageRoot();

    @Autowired
    private LocalContentSeeder seeder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private CommentRepository commentRepository;

    @DynamicPropertySource
    static void configureStorage(DynamicPropertyRegistry registry) {
        registry.add("local.storage.base-path", STORAGE_ROOT::toString);
    }

    @AfterAll
    static void removeAssets() throws IOException {
        try (Stream<Path> paths = Files.walk(STORAGE_ROOT)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to remove seeded asset: " + path, exception);
                }
            });
        }
    }

    @Test
    @Transactional
    void seedsNaturalContentAssetsOnceAndSupportsListAndDetailQueries() throws Exception {
        Counts initial = counts();

        seeder.seed();
        seeder.seed();

        assertThat(counts()).isEqualTo(initial);
        assertThat(initial.users()).isGreaterThanOrEqualTo(5);
        assertThat(initial.posts()).isGreaterThanOrEqualTo(8);
        assertThat(initial.sheetPosts()).isGreaterThanOrEqualTo(6);
        assertThat(userRepository.findByEmail(LocalContentSeeder.SENTINEL_EMAIL)).isPresent();

        List<User> users = userRepository.findAll(PageRequest.of(0, 20));
        List<Post> posts = postRepository.findAll(
                PageRequest.of(0, 20), Sort.by(Sort.Direction.DESC, "createdAt"));
        List<SheetPost> sheetPosts = sheetPostRepository.findAll(PageRequest.of(0, 20)).getContent();

        assertThat(users).hasSizeGreaterThanOrEqualTo(5);
        assertThat(posts).hasSizeGreaterThanOrEqualTo(8);
        assertThat(sheetPosts).hasSizeGreaterThanOrEqualTo(6);
        assertThat(postRepository.findById(posts.get(0).getId())).isPresent();
        assertThat(sheetPostRepository.findById(sheetPosts.get(0).getId())).isPresent();

        assertNaturalContent(users, posts, sheetPosts);
        assertComments(posts, sheetPosts);
        assertAssets(users, sheetPosts);
    }

    @Test
    void productionProfileDoesNotCreateSeedBeans() {
        new ApplicationContextRunner()
                .withUserConfiguration(LocalContentSeedConfiguration.class)
                .withPropertyValues("spring.profiles.active=prod")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LocalContentSeeder.class);
                    assertThat(context).doesNotHaveBean(CommandLineRunner.class);
                });
    }

    private void assertNaturalContent(List<User> users, List<Post> posts, List<SheetPost> sheetPosts) {
        Stream<String> userFields = users.stream().flatMap(user -> Stream.of(
                user.getName(), user.getSecurityUser().getUsername(), user.getProfileSrc()));
        Stream<String> postFields = posts.stream().flatMap(post -> Stream.of(post.getTitle(), post.getContent()));
        Stream<String> sheetFields = sheetPosts.stream().flatMap(sheetPost -> Stream.of(
                sheetPost.getTitle(),
                sheetPost.getContent(),
                sheetPost.getSheet().getTitle(),
                sheetPost.getSheet().getOriginalFileName(),
                sheetPost.getSheet().getSheetUrl(),
                sheetPost.getSheet().getThumbnailUrl()));

        List<String> visibleFields = Stream.of(userFields, postFields, sheetFields)
                .flatMap(stream -> stream)
                .toList();
        assertThat(visibleFields).allSatisfy(value -> {
            assertThat(value).isNotBlank();
            assertThat(FORBIDDEN_MARKER.matcher(value.toLowerCase(Locale.ROOT)).find()).isFalse();
            assertThat(SIMPLE_SEQUENCE.matcher(value).matches()).isFalse();
        });
    }

    private void assertComments(List<Post> posts, List<SheetPost> sheetPosts) {
        List<? extends Article> articles = Stream.concat(posts.stream(), sheetPosts.stream()).toList();
        assertThat(articles).allSatisfy(article -> {
            assertThat(article.getComments()).isNotEmpty();
            assertThat(commentRepository.findAllByTargetId(article.getId(), PageRequest.of(0, 10)))
                    .isNotEmpty();
        });
    }

    private void assertAssets(List<User> users, List<SheetPost> sheetPosts) throws Exception {
        for (User user : users) {
            Path profile = localAsset(user.getProfileSrc());
            assertThat(profile).exists().isRegularFile();
            assertThat(Files.readString(profile)).contains("<svg");
        }

        for (SheetPost sheetPost : sheetPosts) {
            Path thumbnail = localAsset(sheetPost.getSheet().getThumbnailUrl());
            Path sheet = localAsset(sheetPost.getSheet().getSheetUrl());
            assertThat(thumbnail).exists().isRegularFile();
            assertThat(Files.readString(thumbnail)).contains("<svg");
            assertThat(sheet).exists().isRegularFile();
            try (PDDocument document = Loader.loadPDF(sheet.toFile())) {
                assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            }
        }
    }

    private Path localAsset(String url) {
        String relativePath = URI.create(url).getPath().replaceFirst("^/", "");
        return STORAGE_ROOT.resolve(relativePath).normalize();
    }

    private Counts counts() {
        return new Counts(userRepository.count(), postRepository.count(), sheetPostRepository.count());
    }

    private static Path createStorageRoot() {
        try {
            return Files.createTempDirectory("mypiano-content-seed-");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private record Counts(long users, long posts, long sheetPosts) {
    }
}
