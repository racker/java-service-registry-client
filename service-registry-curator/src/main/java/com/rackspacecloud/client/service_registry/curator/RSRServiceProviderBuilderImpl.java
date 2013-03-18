package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.x.discovery.ProviderStrategy;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceProvider;
import com.netflix.curator.x.discovery.ServiceProviderBuilder;

import java.util.concurrent.ThreadFactory;

// very closely resembles ServiceProviderBuilderImpl

public class RSRServiceProviderBuilderImpl<T> implements ServiceProviderBuilder<T> {
    private final ServiceDiscovery<T> discovery;
    
    private String serviceName;
    private ProviderStrategy<T> providerStrategy;
    private ThreadFactory threadFactory;
    private int refreshPaddingMs;
    
    public RSRServiceProviderBuilderImpl(ServiceDiscovery<T> discovery) {
        this.discovery = discovery;
    }
    
    public ServiceProvider<T> build() {
        ServiceCache<T> cache = discovery.serviceCacheBuilder().name(serviceName).threadFactory(threadFactory).build();
        return new RSRServiceProviderImpl(cache, this.providerStrategy);
    }

    public ServiceProviderBuilder<T> serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceProviderBuilder<T> providerStrategy(ProviderStrategy<T> providerStrategy) {
        this.providerStrategy = providerStrategy;
        return this;
    }

    public ServiceProviderBuilder<T> threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ServiceProviderBuilder<T> refreshPaddingMs(int refreshPaddingMs) {
        this.refreshPaddingMs = refreshPaddingMs;
        return this;
    }
}
