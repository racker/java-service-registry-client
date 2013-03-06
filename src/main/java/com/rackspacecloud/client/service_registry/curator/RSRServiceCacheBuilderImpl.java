package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceCacheBuilder;

import java.util.concurrent.ThreadFactory;

public class RSRServiceCacheBuilderImpl<T> implements ServiceCacheBuilder<T> {
    private final RSRServiceDiscoveryImpl<T> discovery;
    private String name;
    private ThreadFactory threadFactory;
    
    public RSRServiceCacheBuilderImpl(RSRServiceDiscoveryImpl discovery) {
        this.discovery = discovery;
    }
    
    public ServiceCache<T> build() {
        return new RSRServiceCacheImpl<T>(this.discovery);
    }

    public ServiceCacheBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public ServiceCacheBuilder<T> threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }
}
