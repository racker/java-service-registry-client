package com.rackspacecloud.client.service_registry.curator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.curator.framework.listen.ListenerContainer;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.details.ServiceCacheListener;
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.objects.Event;
import com.rackspacecloud.client.service_registry.objects.EventType;
import com.rackspacecloud.client.service_registry.objects.ServicePayload;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class RSRServiceCacheImpl<T> implements ServiceCache<T> {
    
    private final RSRServiceDiscoveryImpl<T> discovery;
    private final ListenerContainer<ServiceCacheListener> listenerContainer = new ListenerContainer<ServiceCacheListener>();
    private final ConcurrentMap<String, ServiceInstance<T>> instances = Maps.newConcurrentMap();
    private final String name;
    
    private volatile Thread eventPoller = null;
    private volatile boolean shouldStopPolling = false;
    
    public RSRServiceCacheImpl(RSRServiceDiscoveryImpl<T> discovery, String name) {
        this.discovery = discovery;
        this.name = name;
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
                while (!shouldStopPolling) {
                    pollAndProcessEvents();
                }
                eventPoller = null;
                shouldStopPolling = false;
            }
        };
        
        // backfill existing services.
        List<String> names = new ArrayList<String>();
        if (this.name != null) {
            names.add(this.name);
        } else {
            for (String name : discovery.queryForNames()) {
                names.add(name);
            }
        }
        for (String name : names) {
            for (ServiceInstance<T> instance : discovery.queryForInstances(name)) {
                instances.put(instance.getId(), instance);
            }
        }
        
        // start the polling thread.
        eventPoller.start();
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
    
    private boolean isPertinent(Event e) {
        return (e.getPayload() instanceof ServicePayload)
               && ((ServicePayload)e.getPayload()).getService().getTags().contains(discovery.getType());
    }
    
    private String processEvents(List<Event> events) {
        String lastId = null;
        for (Event e : events) {
            lastId = e.getId();
            if (!isPertinent(e)) {
                continue;
            }
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
                final ServicePayload ee = (ServicePayload)e.getPayload();
                if (name == null || (name.equals(ee.getService().getMetadata().get("name")))) {
                    try {
                        if (e.getType() == EventType.SERVICE_JOINED) {
                            instances.put(ee.getService().getId(), discovery.convert(ee.getService()));
                        } else if (e.getType() == EventType.SERVICE_REMOVED || e.getType() == EventType.SERVICE_TIMEOUT) {
                            instances.remove(ee.getService().getId());
                        }
                    } catch (Exception fromConverter) {
                        // todo: log it.
                    }
                }
            } catch (Throwable th) {
                // bad listener! todo: log only.
            }
        }
        return lastId;
    }
    
    // pull a page of events, process that page, pull the next page, etc.
    // todo: switch to iterator.
    private void pollAndProcessEvents() {
        PaginationOptions options = new PaginationOptions(100, null);
        List<Event> events = null;
        int count = 0;
        do {
            if (shouldStopPolling) break;
            count += 1;
            try {
                // get events from server, filter the ones we are interested in.
                events = Lists.newArrayList(discovery.getClient().getEventsClient().list(options));
                String lastEventId = processEvents(events);
                options = options.withMarker(lastEventId);
            } catch (Exception ex) {
                // todo: just log it.
                events = null;
            }
        } while (events != null && events.size() > 0);
        
        // if it only happened once, assume there are not many events happening.
        if (count == 1) {
            // todo: trace message.
            // todo: this should become configurable.
            try { Thread.sleep(1000); } catch (Exception ex) {}
        }
    }
}
