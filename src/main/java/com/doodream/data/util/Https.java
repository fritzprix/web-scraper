package com.doodream.data.util;

import io.reactivex.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class Https {

    private static final String AGENT_KEY = "User-Agent";
    private static final String AGENT_PROPERTY = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";

    public static Observable<String> getResponse(String urlString) {

        Observable<URL> urlObservable = Observable.just(urlString)
                .map(URL::new).cache();

        // if target server response with intermediate ad. page based on browser information
        // while accept non-browser connection(e.g. forbes.com ...)
        Observable<HttpURLConnection> nonBrowserUrlConnectionObservable = urlObservable
                .flatMap(url -> buildConnection(url, false))
                .doOnNext(URLConnection::connect);

        Observable<HttpURLConnection> browserUrlConnectionObservable = urlObservable
                .flatMap(url -> buildConnection(url, true))
                .doOnNext(URLConnection::connect);


        // then try to get html document with non-browser request (means no user-agent property)
        return nonBrowserUrlConnectionObservable
                .map(URLConnection::getInputStream)
                // if the request is not successful then try to get page mimicking browser
                .onErrorResumeNext(browserUrlConnectionObservable.map(URLConnection::getInputStream))
                .map(Https::toHtmlString);


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

    private static <R> String toHtmlString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine().concat("\n"));
        }
        return stringBuilder.toString();
    }


    private static boolean isForbidden(HttpURLConnection httpURLConnection) throws IOException {
        return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN;
    }

    private static boolean isSuccessful(HttpURLConnection httpURLConnection) throws IOException {
        return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
}
