package com.doodream.data.dagger;

import com.doodream.data.client.ReactiveClient;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = AsyncSupportModule.class)
public interface ClientComponent {
    void inject(ReactiveClient client);
}
