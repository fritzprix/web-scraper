package com.doodream.data.client.model.news;

import com.doodream.data.util.net.HttpRequest;
import com.google.common.base.Strings;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observables.GroupedObservable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NewsContent {
    private static final Pattern TAG_MATCHER = Pattern.compile("\\<[^><]+\\>");
    private static final Pattern ARTICLE_MATCHER = Pattern.compile("\\<article[^<>]+\\>([\\s\\S]+)\\<\\/article\\>");
    private static final Pattern DATE_MATCHER = Pattern.compile("[a-zA-Z]+,([\\s\\S]+)\\s?GMT");
    private static final Pattern ADVERTISER = Pattern.compile("([.]+)?[Aa]dvertisement\\s?[.]+");
    private static final Pattern HOST_TO_SOURCE = Pattern.compile("(w+\\.)?(\\S\\S\\.)?([^\\.]+)\\.[\\s\\S]+");

    private static final String CSS_SELECT_META = "meta";
    private static final String CSS_SELECT_PARAGRAPH = "p";

    private static final String UNKNOWN_VALUE = "UNKNOWN";
    private static final String ANY_NONE_ALPHA = "[^a-zA-Z]+";

    private static final String ATTR_CONTENT = "content";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PROP = "property";

    private static final String DATE_PATTERN = "dd MMM yyyy hh:mm:ss";

    private static final String AGENT_KEY = "User-Agent";
    private static final String AGENT_PROPERTY = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";


    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<>();

    String source;
    String author;
    String url;
    String title;
    Long pubDateInEpoch;
    String description;
    String category;
    String body;


    public static Observable<NewsContent> extractNewsContents(GoogleNewsRSS googleNewsRSS) {


        return Observable.fromIterable(googleNewsRSS.getChannel().getItems())
                .groupBy(GoogleNewsRSSItem::getLink)
                .flatMap(NewsContent::buildFromUrl)
                .filter(NewsContent::mustBodyNotEmpty)
                .filter(NewsContent::mustNotHiddenByAdvertiser);

    }

    private static boolean mustNotHiddenByAdvertiser(NewsContent content) {
        return !ADVERTISER.matcher(content.getBody()).find();
    }

    private static <R> Observable<NewsContent> buildFromUrl(GroupedObservable<String, GoogleNewsRSSItem> observable) {
        return HttpRequest.getResponse(observable.getKey())
                .zipWith(observable, NewsContent::build);
    }

    private static NewsContent build(String htmlText, GoogleNewsRSSItem newsItem) {
        Document document = Jsoup.parse(htmlText);
        Elements meta = document.select(CSS_SELECT_META);

        return Observable.just(NewsContent.builder())
                .doOnNext(newsContentBuilder -> newsContentBuilder.title(newsItem.getTitle()))
                .doOnNext(newsContentBuilder -> newsContentBuilder.url(newsItem.getLink()))
                .doOnNext(newsContentBuilder -> newsContentBuilder.category(newsItem.getCategory()))
                .zipWith(NewsContent.extractEpoch(newsItem.getPubDate()), NewsContentBuilder::pubDateInEpoch)
                .zipWith(extractSource(meta, URI.create(newsItem.getLink()).getHost()), NewsContentBuilder::source)
                .zipWith(extractAuthor(meta), NewsContentBuilder::author)
                .zipWith(extractDescription(meta), NewsContentBuilder::description)
                .zipWith(extractArticle(document), NewsContentBuilder::body)
                .map(NewsContentBuilder::build)
                .blockingSingle();

    }

    private static boolean mustBodyNotEmpty(NewsContent content) {
        return !Strings.isNullOrEmpty(content.getBody());
    }

    private static Observable<String> extractAuthor(Elements meta) {
        return Observable.fromIterable(meta)
                .filter(NewsContent::hasAuthor)
                .map(element -> element.attr(ATTR_CONTENT))
                .map(NewsContent::avoidEmpty)
                .map(NewsContent::tripLineSeparator)
                .defaultIfEmpty(UNKNOWN_VALUE)
                .map(String::toUpperCase);
    }

    private static <R> String tripLineSeparator(String s) {
        return s.replaceAll("\\n[\\s\\S]+","");
    }

    private static String avoidEmpty(String s) {
        if(s.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        return s;
    }


    private static Observable<String> extractSource(Elements meta, String host) {

        Observable<String> sourceObservable = Observable.fromIterable(meta)
                .filter(NewsContent::hasSourceInfo)
                .map(element -> element.attr(ATTR_CONTENT));

        return Observable.just(host)
                .map(HOST_TO_SOURCE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(3))
                .onErrorResumeNext(sourceObservable)
                .map(String::toUpperCase);

    }


    private static Observable<String> extractArticle(Document document) {
        return Observable.fromIterable(document.body().select(CSS_SELECT_PARAGRAPH))
                .map(Element::text)
                .map(NewsContent::lineSeparatorToBlank)
                .reduce(String::concat)
                .defaultIfEmpty("")
                .toObservable();
    }

    private static String lineSeparatorToBlank(String s) {
        return s.replaceAll("\n"," ");
    }

    private static Observable<Long> extractEpoch(String pubDate) {

        if(dateFormatThreadLocal.get() == null) {
            dateFormatThreadLocal.set(new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH));
        }

        return Single.just(DATE_MATCHER.matcher(pubDate))
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(String::trim)
                .map((date) -> dateFormatThreadLocal.get().parse(date))
                .map(Date::toInstant)
                .map(Instant::getEpochSecond)
                .defaultIfEmpty(-1L)
                .toObservable();
    }


    private static Observable<String> extractDescription(Elements meta) {
        HashSet<Integer> duplicationSearchSet = new HashSet<>();

        return Observable.fromIterable(meta)
                .filter(NewsContent::hasDescription)
                .map(element -> element.attr(ATTR_CONTENT))
                .filter(s -> duplicationSearchSet.add(s.hashCode()))
                .map(NewsContent::lineSeparatorToBlank)
                .reduce(String::concat)
                .defaultIfEmpty("")
                .toObservable();
    }

    private static boolean hasDescription(Element element) {
        return element.attr(ATTR_NAME).equalsIgnoreCase("description")
                || element.attr(ATTR_PROP).contains("description");
    }

    // for example, reuters provides U.S as a content for meta tag of og.site_name
    private static boolean hasSourceInfo(Element element) {
        return element.attr(ATTR_NAME).contains("site_name")
                || element.attr(ATTR_PROP).contains("site_name");
    }

    private static boolean hasAuthor(Element element) {
        return element.attr(ATTR_NAME).equalsIgnoreCase("author")
                || element.attr(ATTR_PROP).equalsIgnoreCase("author");
    }

}
