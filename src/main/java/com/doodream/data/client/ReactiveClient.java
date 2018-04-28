package com.doodream.data.client;

import com.google.gson.Gson;
import io.reactivex.Scheduler;
import lombok.Data;
import okhttp3.OkHttpClient;

import javax.inject.Inject;

@Data
public class ReactiveClient {

    @Inject
    Scheduler scheduler;

    @Inject
    Gson gson;

    @Inject
    OkHttpClient okHttpClient;
}
