package com.rackspacecloud.client.service_registry.curator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.curator.framework.listen.ListenerContainer;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.details.ServiceCacheListener;
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.events.server.AbstractServiceEvent;
import com.rackspacecloud.client.service_registry.events.server.BaseEvent;
import com.rackspacecloud.client.service_registry.events.server.ServiceJoinEvent;
import com.rackspacecloud.client.service_registry.events.server.ServiceTimeoutEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class RSRServiceCacheImpl<T> implements ServiceCache<T> {
    
    private final RSRServiceDiscoveryImpl<T> discovery;
    private final ListenerContainer<ServiceCacheListener> listenerContainer = new ListenerContainer<ServiceCacheListener>();
    private final ConcurrentMap<String, ServiceInstance<T>> instances = Maps.newConcurrentMap();
    
    private volatile Thread eventPoller = null;
    private volatile boolean shouldStopPolling = false;
    
    public RSRServiceCacheImpl(RSRServiceDiscoveryImpl<T> discovery) {
        this.discovery = discovery;
    }
    
    //
    // ServiceCache methods (includes method of InstanceProvider<T>)
    //
    
    public List<ServiceInstance<T>> getInstances() {
        return Collections.unmodifiableList(Lists.newArrayList(instances.values()));
    }

    public synchronized void start() throws Exception {
        if (eventPoller != null) return;
        shouldStopPolling = false;
        eventPoller = new Thread("Event poller for " + discovery.getClient()) {
            @Override
            public void run() {
                PaginationOptions options = new PaginationOptions(100, null);
                while (!shouldStopPolling) {
                    List<BaseEvent> events = null;
                    do {
                        if (shouldStopPolling) break;
                        try {
                            // get events from server, filter the ones we are interested in.
                            events = discovery.getClient().getEventsClient().list(options);
                            for (BaseEvent e : events) {
                                if (isPertinent(e)) {
                                    // ideally, the dispatch happens on a separate thread.
                                    try {
                                        // listeners should do work.
                                        listenerContainer.forEach(new Function<ServiceCacheListener, Void>() {
                                            public Void apply(@Nullable ServiceCacheListener serviceCacheListener) {
                                                if (serviceCacheListener == null) return null;
                                                serviceCacheListener.cacheChanged();
                                                return null;
                                            }
                                        });
                                        
                                        // make sure the local collection is maintained.
                                        final AbstractServiceEvent ee = (AbstractServiceEvent)e;
                                        try {
                                            if (ee instanceof ServiceJoinEvent) {
                                                instances.put(ee.getService().getId(), discovery.convert(ee.getService()));
                                            } else if (ee instanceof ServiceTimeoutEvent) {
                                                instances.remove(ee.getService().getId());
                                            }
                                        } catch (Exception fromConverter) {
                                            // todo: log it.
                                        }
                                    } catch (Throwable th) {
                                        // bad listener! todo: log only.
                                    }
                                }
                                options = options.withMarker(e.getId());
                            }                            
                        } catch (Exception ex) {
                            // todo: just log it.
                            events = null;
                        }
                    } while (events != null && events.size() > 0);
                }
                eventPoller = null;
                shouldStopPolling = false;
            }
        };
    }

    //
    // java.io.Closeable
    //
    
    public synchronized void close() throws IOException {
        // again, nothing to do here.
        shouldStopPolling = true;
    }
    
    //
    // com.netflix.curator.framework.listen.Listenable
    //

    public void addListener(ServiceCacheListener serviceCacheListener) {
        listenerContainer.addListener(serviceCacheListener);
    }

    public void addListener(ServiceCacheListener serviceCacheListener, Executor executor) {
        listenerContainer.addListener(serviceCacheListener, executor);
    }

    public void removeListener(ServiceCacheListener serviceCacheListener) {
        listenerContainer.removeListener(serviceCacheListener);
    }
    
    // helpers
    
    private boolean isPertinent(BaseEvent e) {
        return e instanceof AbstractServiceEvent && ((AbstractServiceEvent)e).getService().getTags().contains(discovery.getType());
    }
}
