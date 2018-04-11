package com.doodream.data.dagger;

import com.doodream.data.client.AsyncWebClient;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = AsyncSupportModule.class)
public interface ClientComponent {
    void inject(AsyncWebClient client);
}
