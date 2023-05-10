package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.ebean.PagedList;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlController {

    public static Handler addUrl = ctx -> {

        String inputUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);

        URL newUrl;
        try {
            newUrl = new URL(inputUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String parsedUrl = newUrl.getProtocol() + "://" + newUrl.getAuthority();

        boolean urlFound = new QUrl()
                .name.equalTo(parsedUrl)
                .exists();

        if (urlFound) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
        } else {
            Url validUrl = new Url(parsedUrl);
            validUrl.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls");
        }
    };

    public static Handler showUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

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

    public static Handler checkUrl  = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        try {
            UrlCheck urlCheck = doUrlCheck(url);
            url.getUrlChecks().add(urlCheck);
            url.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };

    public static UrlCheck doUrlCheck(Url url) {

        HttpResponse<String> response = Unirest
                .get(url.getName())
                .asString();

        String body = response.getBody();
        Document doc = Jsoup.parse(body);

        int statusCode = response.getStatus();
        String title = doc.title();

        Element h1tag = doc.selectFirst("h1");
        String h1 = h1tag != null ? h1tag.text() : "";

        Element descriptionElement = doc.selectFirst("meta[name=description]");
        String description = descriptionElement == null ? "" : descriptionElement.attr("content");
        return new UrlCheck(statusCode, title, h1, description);
    }
}
