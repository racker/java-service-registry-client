package com.rackspacecloud.client.service_registry.rsr_events;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceTimeoutEvent extends BaseEvent {
    private Service service;

    public Service getService() {
        return service;
    }
}
