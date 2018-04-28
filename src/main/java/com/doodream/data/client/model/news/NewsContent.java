package com.doodream.data.client.model.news;

import io.reactivex.Observable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.regex.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NewsContent {
    private static final Pattern TAG_MATCHER = Pattern.compile("\\<[^><]+\\>");
    private static final Pattern ARTICLE_MATCHER = Pattern.compile("\\<article[^<>]+\\>([\\s\\S]+)\\<\\/article\\>");
    private static final Pattern DATE_MATCHER = Pattern.compile("([\\s\\S]+)\\s?GMT");
    private static final String DATE_PATTERN = "EEE, dd MMM yyyy hh:mm:ss";
    private static final String AGENT_PROPERTY[] = {
            "User-Agent",
            "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"
    };


    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<>();

    String source;
    String url;
    String title;
    Long pubDateInEpoch;
    String description;
    String category;
    String body;


    public static Observable<NewsContent> extractNewsContents(GoogleNewsRSS googleNewsRSS) {

        Observable<GoogleNewsRSSItem> itemsObservable = Observable.fromIterable(googleNewsRSS.getChannel().getItems());

        Observable<HttpURLConnection> urlObservable = itemsObservable
                .map(GoogleNewsRSSItem::getLink)
                .map(URL::new)
                .map(URL::openConnection)
                .cast(HttpURLConnection.class)
                .doOnNext(HttpURLConnection::connect);

        Observable<HttpURLConnection> connectionObservable = urlObservable
                .filter(NewsContent::isSuccessful);

        Observable<HttpURLConnection> agentCheckConnectionObservable = urlObservable
                .filter(httpURLConnection -> !NewsContent.isSuccessful(httpURLConnection)) //
                .doOnNext(HttpURLConnection::disconnect)
                .map(URLConnection::getURL)
                .map(URL::openConnection)
                .cast(HttpURLConnection.class)
                .doOnNext(httpURLConnection -> httpURLConnection.setRequestProperty(AGENT_PROPERTY[0], AGENT_PROPERTY[1]))
                .doOnNext(URLConnection::connect)
                .filter(NewsContent::isSuccessful);

        Observable<String> contentObservable = agentCheckConnectionObservable.mergeWith(connectionObservable)
                .map(HttpURLConnection::getInputStream)
                .map(NewsContent::toHtmlString)
                .map(Jsoup::parse)
                .map(document -> document.select("p"))
                .map(NewsContent::extractValue);

        return itemsObservable
                .map(NewsContent::fromRssItem)
                .zipWith(contentObservable, (newsContent, s) -> {
                    newsContent.setBody(s);
                    return newsContent;
                });
    }

    private static boolean isSuccessful(HttpURLConnection httpURLConnection) throws IOException {
        return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }


    private static <R> String toHtmlString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine().concat("\n"));
        }
        return stringBuilder.toString();
    }

    private static <R> String extractValue(Elements elements) {

        return Observable.fromIterable(elements).map(Element::text).reduce(String::concat).blockingGet();
    }


    private static <R> NewsContent fromRssItem(GoogleNewsRSSItem googleNewsRSSItem) {

        return NewsContent.builder()
                .url(googleNewsRSSItem.link)
                .source(googleNewsRSSItem.link)
                .title(googleNewsRSSItem.title)
                .category(googleNewsRSSItem.category)
                .build();
    }
}
