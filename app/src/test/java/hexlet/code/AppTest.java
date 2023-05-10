package hexlet.code;

import hexlet.code.domain.UrlCheck;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockWebServer;
import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer server;

    static String readFile(String fileName) throws IOException {
        Path filePath = Paths.get("src", "test", "resources", fileName);
        return Files.readString(filePath);
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        server = new MockWebServer();
        String testPage = readFile("testpage.html");
        server.enqueue(new MockResponse().setBody(testPage));
        server.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        server.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed.sql");
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest
                .get(baseUrl)
                .asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testUrls() {

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("https://youtube.com");
        assertThat(content).contains("https://ru.hexlet.io");
    }

    @Test
    void testUrl() {

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/2")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("https://ru.hexlet.io");
        assertThat(content).doesNotContain("https://youtube.com");
    }

    @Test
    void testAddValidUrl() {

        String inputUrl = "https://twitter.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(inputUrl);
        assertThat(content).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
                .name.equalTo(inputUrl)
                .findOne();

        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo(inputUrl);
    }

    @Test
    void testAddInvalidUrl() {

        String inputUrl = "www.twitter.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).doesNotContain(inputUrl);
        assertThat(content).contains("Некорректный URL");

        Url actualUrl = new QUrl()
                .name.equalTo(inputUrl)
                .findOne();

        assertThat(actualUrl).isNull();
    }

    @Test
    void testReinsertion() {

        String inputUrl = "https://gmail.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(inputUrl);
        assertThat(content).contains("Страница уже существует");
    }

    @Test
    void testCheckUrl() {
        String mockUrl = server.url("/").toString().replaceAll("/$", "");

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asEmpty();

        Url url = new QUrl()
                .name.equalTo(mockUrl)
                .findOne();

        assertThat(url).isNotNull();

        List<Url> list = new QUrl().findList();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(list.size()).isEqualTo(4);

        HttpResponse responseCheck = Unirest
                .post(baseUrl + "/urls/4/checks")
                .asEmpty();

        assertThat(responseCheck.getStatus()).isEqualTo(302);

        UrlCheck urlCheck = url.getUrlChecks().get(0);
        assertThat(urlCheck.getStatusCode()).isEqualTo(200);
        assertThat(urlCheck.getTitle()).isEqualTo("Some title");
        assertThat(urlCheck.getH1()).isEqualTo("Some header");
    }
}
