package hexlet.code;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
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
                .get(baseUrl + "/articles/2")
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
        assertThat(content).contains("The page has been successfully added");

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
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(inputUrl);
        assertThat(content).contains("Invalid URL");

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
        assertThat(content).contains("The page already exists");
    }
}
