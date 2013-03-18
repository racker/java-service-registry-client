package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.x.discovery.ProviderStrategy;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceProvider;

import java.io.IOException;

public class RSRServiceProviderImpl<T> implements ServiceProvider<T> {
    private final ServiceCache<T> cache;
    private final ProviderStrategy<T> providerStrategy;
    
    public RSRServiceProviderImpl(ServiceCache<T> cache, ProviderStrategy<T> providerStrategy) {
        this.cache = cache;
        this.providerStrategy = providerStrategy;
    }
    
    public void start() throws Exception {
        cache.start();
    }

    public ServiceInstance<T> getInstance() throws Exception {
        return providerStrategy.getInstance(cache);
    }

    public void close() throws IOException {
        cache.close();
    }
}
