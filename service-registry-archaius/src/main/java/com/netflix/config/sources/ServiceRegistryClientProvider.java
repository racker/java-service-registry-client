package com.netflix.config.sources;

import com.google.common.collect.Lists;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceRegistryClientProvider implements ServiceRegistryClient {
    private final Client client;

    public ServiceRegistryClientProvider(String user, String key, String region) {
        client = new Client(user, key, region);
    }

    @Override
    public List<Service> getServices(String tag) throws Exception {
        List<Service> services = new ArrayList<Service>();
        Iterator<Service> it = client.getServicesClient().list(new PaginationOptions(100, null), tag); 
        while (it.hasNext()) {
            services.add(it.next());
        }
        return services;
    }

    @Override
    public List<ConfigurationValue> getConfiguration() throws Exception {
        return Lists.newArrayList(client.getConfigurationClient().list(null));
    }
}
