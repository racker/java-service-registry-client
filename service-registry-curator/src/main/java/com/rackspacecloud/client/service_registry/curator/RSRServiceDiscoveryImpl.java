package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.utils.ThreadUtils;
import com.netflix.curator.x.discovery.ServiceCacheBuilder;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceProviderBuilder;
import com.netflix.curator.x.discovery.strategies.RoundRobinStrategy;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.MethodOptions;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public class RSRServiceDiscoveryImpl<T> implements ServiceDiscovery<T> {
    
    private final Client client;
    private final Class<T> typeClass;
    private final String typeTag;
    private final Method convert;
    
    private final Map<String, ServiceTracker<T>> services = new HashMap<String, ServiceTracker<T>>(); // needs synchronized
        
    public RSRServiceDiscoveryImpl(Client client, Class<T> type) {
        // deep validation has already been done.
        try {
            Method m = type.getMethod("convert", Service.class);
            m.setAccessible(true);
            convert = m;
        } catch (NoSuchMethodException ex) {
            throw new MissingResourceException("Class does not implement static convert() method", type.getName(), "convert");
        } catch (Exception ex) {
            throw new MissingResourceException(ex.getMessage(), type.getName(), "convert");
        }
        
        this.client = client;
        this.typeClass = type;
        this.typeTag = Utils.sanitizeTag(type.getName());
    }
    public void start() throws Exception {
        // noop
    }

    public synchronized void registerService(ServiceInstance<T> service) throws Exception {
        ServiceTracker<T> tracker = services.get(service.getId());
        if (tracker == null) {
            tracker = new ServiceTracker<T>(client, service, typeTag);
            tracker.register();
            services.put(service.getId(), tracker);
        }
    }

    public void updateService(ServiceInstance<T> service) throws Exception {
        // todo: consider the implications of this implementation. unregistration forces some heartbeat events
        unregisterService(service);
        registerService(service);
    }

    public void unregisterService(ServiceInstance<T> service) throws Exception {
        client.getServicesClient().delete(service.getId());
        services.remove(service.getId()).stop();
    }

    public ServiceCacheBuilder<T> serviceCacheBuilder() {
        return new RSRServiceCacheBuilderImpl<T>(this)
                .threadFactory(ThreadUtils.newThreadFactory("RSRServiceCache"));
        // todo: whither 'name'?
    }

    /** return all distinct names registered by this discovery type. */
    public Collection<String> queryForNames() throws Exception {
        Set<String> names = new HashSet<String>();
        // todo: it would be better to do:
        // services = client.getServicesClient().list(options, typeTag);
        // but there are some validation problems (the tag is allowed to be written, but not queried on).
        Iterator<Service> services = client.getServicesClient().list(new MethodOptions(100, null));
        
        while (services.hasNext()) {
            Service service = services.next();
            // this conditional can be removed when the above operation works.
            if (!service.getTags().contains(typeTag)) {
                continue;
            }
            String name = service.getMetadata().get(ServiceTracker.NAME);
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /** return all instances registered to this particular name for this discovery type */
    public Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception {
        List<ServiceInstance<T>> serviceInstances = new ArrayList<ServiceInstance<T>>();
        Iterator<Service> services = client.getServicesClient().list(new MethodOptions(100, null));
        while (services.hasNext()) {
            Service service = services.next();
            if (service.getTags().contains(typeTag) && service.getMetadata().get(ServiceTracker.NAME).equals(name)) {
                // does the job of the serializer in the curator code (theirs is just a json marshaller anyway).
                serviceInstances.add(convert(service));
            }
        }
        return serviceInstances;
    }

    public ServiceInstance<T> queryForInstance(String name, String id) throws Exception {
        return (ServiceInstance<T>) convert.invoke(typeClass, client.getServicesClient().get(id));
    }

    public ServiceProviderBuilder<T> serviceProviderBuilder() {
        return new RSRServiceProviderBuilderImpl<T>(this)
                // todo: what about these pieces?
                //.refreshPaddingMs(1000)
                //.serviceName("foo")
                .providerStrategy(new RoundRobinStrategy<T>())
                .threadFactory(ThreadUtils.newThreadFactory("RSRServiceProvider")); 
    }

    public synchronized void close() throws IOException {
        for (ServiceTracker<T> tracker : services.values()) {
            tracker.stop();
        }
        services.clear();
    }
    
    //
    // helpers
    //
    
    public Client getClient() { return client; }
    public String getType() { return typeTag; }
    
    public ServiceInstance<T> convert(Service service) throws Exception {
        return (ServiceInstance<T>) convert.invoke(typeClass, service);
    }
}
