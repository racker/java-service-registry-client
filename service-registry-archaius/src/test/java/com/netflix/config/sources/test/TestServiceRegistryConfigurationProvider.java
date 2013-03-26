package com.netflix.config.sources.test;

import com.netflix.config.sources.ServiceRegistryClient;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import com.rackspacecloud.client.service_registry.objects.Event;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.util.*;

public class TestServiceRegistryConfigurationProvider implements ServiceRegistryClient {

    Map<String, List<Service>> services = new HashMap<String, List<Service>>();
    List<ConfigurationValue> configurationValues = new ArrayList<ConfigurationValue>();

    public void addService(String tag, Service svc) {
        List<Service> svcs = services.get(tag);
        if (svcs == null) {
            svcs = new ArrayList<Service>();
        }
        svcs.add(svc);
        services.put(tag, svcs);
    }

    public void addConfigurationValue(ConfigurationValue cfg) {
        configurationValues.add(cfg);
    }

    @Override
    public List<Service> getServices(String tag) throws Exception {
        List<Service> result = services.get(tag);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    @Override
    public List<ConfigurationValue> getConfiguration() throws Exception {
        return configurationValues;
    }
}
