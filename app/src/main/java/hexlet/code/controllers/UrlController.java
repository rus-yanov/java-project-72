package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.ebean.PagedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlController {

    public static Handler addUrl = ctx -> {

        String parsedUrl = parseUrl(ctx.formParam("url"));

        if (parsedUrl.equals("invalid URL")) {
            ctx.sessionAttribute("flash", "Invalid URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url url = new QUrl()
                .name.equalTo(parsedUrl)
                .findOne();

        if (url == null) {
            Url newUrl = new Url(parsedUrl);
            newUrl.save();
            ctx.sessionAttribute("flash", "The page has been successfully added");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls");
        } else {
            ctx.sessionAttribute("flash", "The page already exists");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
        }
    };

    public static Handler showUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedArticles = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedArticles.getList();

        int lastPage = pagedArticles.getTotalPageCount() + 1;
        int currentPage = pagedArticles.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler urlCheck  = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }


    };

    public static String parseUrl(String url) throws MalformedURLException {
        if (!url.startsWith("http") || !url.startsWith("https")) {
            return "invalid URL";
        } else {
            URL wholeUrl = new URL(url);
            return wholeUrl.getProtocol() + "://" + wholeUrl.getAuthority();
        }
    }
}
