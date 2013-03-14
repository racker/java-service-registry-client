package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.Region;
import com.rackspacecloud.client.service_registry.clients.BaseClient;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.MissingResourceException;

public class RSRServiceDiscoveryBuilder<T> {
    private final Class<T> type;
    
    private String apiKey;
    private String user;
    private String apiUrl = BaseClient.PRODUCTION_URL;
    
    public static<T> RSRServiceDiscoveryBuilder<T> builder(Class<T> cls) {
        // validate this sucker right now.
        for (Class convertParametertype : new Class[] {Service.class, cls}) {
            try {
                Method m = cls.getMethod("convert", convertParametertype);
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new DuckInterfaceException("convert method should be static");
                }
                if (!m.getReturnType().equals(ServiceInstance.class)) {
                    throw new DuckInterfaceException("return type should be ServiceInstance<T>");
                }
            } catch (NoSuchMethodException ex) {
                throw new DuckInterfaceException(String.format("Class does not implement static convert(%s) method", convertParametertype.getSimpleName()));
            }
        }
        return new RSRServiceDiscoveryBuilder<T>(cls);
    }
    
    private RSRServiceDiscoveryBuilder(Class<T> type) {
        this.type = type;
    }
    
    public RSRServiceDiscoveryBuilder<T> withApiKey(String s) {
        this.apiKey = s;
        return this;
    }
    
    public RSRServiceDiscoveryBuilder<T> withUser(String s) {
        this.user = s;
        return this;
    }
    
    public RSRServiceDiscoveryBuilder<T> withApiUrl(String s) {
        this.apiUrl = s;
        return this;
    }
    
    public ServiceDiscovery<T> build() {
        Client client = new Client(this.user, this.apiKey, Region.US, this.apiUrl);
        return new RSRServiceDiscoveryImpl<T>(client, this.type);
    }   
}
