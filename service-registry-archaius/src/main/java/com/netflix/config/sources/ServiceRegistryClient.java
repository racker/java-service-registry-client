package com.netflix.config.sources;

import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.util.List;

public interface ServiceRegistryClient {
    List<Service> getServices(String tag) throws Exception;

    List<ConfigurationValue> getConfiguration() throws Exception;
}
