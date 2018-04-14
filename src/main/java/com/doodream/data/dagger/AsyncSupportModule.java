package com.doodream.data.dagger;

import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Module
public class AsyncSupportModule {

    @Provides
    Executor provideExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Provides
    Scheduler provideScheduler(Executor executor) {
        return Schedulers.from(executor);
    }

    @Provides
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    CookieJar provideCookieJar() {
        return new CookieJar() {
            private HashMap<String, List<Cookie>> cookieCache = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
                System.out.println(cookies.toString());
                cookieCache.putIfAbsent(httpUrl.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                List<Cookie> cookies = cookieCache.get(httpUrl.host());
                if(cookies != null) {
//                    System.out.printf("Requested /w : %s for %s\n", cookies.toString(), httpUrl.toString());
                    return cookies;
                }
                return Collections.EMPTY_LIST;

            }
        };
    }

    @Provides
    OkHttpClient provideOKhttpClient(CookieJar cookieJar) {
        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
    }
}
