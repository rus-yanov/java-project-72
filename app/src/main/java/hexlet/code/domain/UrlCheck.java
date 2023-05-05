package hexlet.code.domain;

import io.ebean.Model;

import javax.persistence.*;
import java.time.Instant;
import io.ebean.annotation.WhenCreated;

@Entity
public class UrlCheck extends Model {

    @Id
    @GeneratedValue
    public long id;

    private int statusCode;

    private String title;

    private String h1;

    @Lob
    private String description;

    @ManyToOne
    private Url url;

    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int statusCode, String title, String h1, String description, Url url) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
    }
}
