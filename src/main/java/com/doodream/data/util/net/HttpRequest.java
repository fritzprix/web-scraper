package com.doodream.data.util.net;

import io.reactivex.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class HttpRequest {

    private static final String AGENT_KEY = "User-Agent";
    private static final String AGENT_PROPERTY = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";

    public static Observable<String> getResponse(String urlString) {

        Observable<URL> urlObservable = Observable.just(urlString)
                .map(URL::new).cache();

        // if target server response with intermediate ad. page based on browser information
        // while accept non-browser connection(e.g. forbes.com ...)
        Observable<HttpURLConnection> nonBrowserUrlConnectionObservable = urlObservable
                .flatMap(url -> buildConnection(url, false))
                .doOnNext(httpURLConnection -> httpURLConnection.setReadTimeout(2000))
                .doOnNext(URLConnection::connect);

        Observable<HttpURLConnection> browserUrlConnectionObservable = urlObservable
                .flatMap(url -> buildConnection(url, true))
                .doOnNext(httpURLConnection -> httpURLConnection.setReadTimeout(2000))
                .doOnNext(URLConnection::connect)
                .filter(HttpRequest::isSuccessful);


        // then try to get html document with non-browser request (means no user-agent property)
        return nonBrowserUrlConnectionObservable
                .map(URLConnection::getInputStream)
                .onErrorResumeNext(browserUrlConnectionObservable.map(URLConnection::getInputStream))
                .map(HttpRequest::toHtmlString)
                .onErrorReturn(throwable -> "");


    }

    private static boolean isSuccessful(HttpURLConnection httpURLConnection) throws IOException {
        return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private static Observable<HttpURLConnection> buildConnection(URL url, boolean browserRequest) {
        Observable<HttpURLConnection> urlConnectionSingle = Observable.just(url)
                .map(URL::openConnection)
                .cast(HttpURLConnection.class);

        if(browserRequest) {
            urlConnectionSingle = urlConnectionSingle
                    .doOnNext(httpURLConnection -> httpURLConnection.setRequestProperty(AGENT_KEY, AGENT_PROPERTY));
        }

        return urlConnectionSingle;
    }

    private static String toHtmlString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine().concat("\n"));
        }
        return stringBuilder.toString();
    }

}
