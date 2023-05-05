package hexlet.code.domain;

import java.time.Instant;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.GeneratedValue;

import io.ebean.Model;
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
