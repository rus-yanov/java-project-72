package hexlet.code.domain;

import io.ebean.Model;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.GeneratedValue;
import javax.persistence.CascadeType;
import java.time.Instant;
import java.util.List;

import io.ebean.annotation.WhenCreated;

@Entity
public class Url extends Model {

    @Id
    @GeneratedValue
    public long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }
}
