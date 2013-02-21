package com.rackspacecloud.client.service_registry.rsr_events;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceJoinEvent extends BaseEvent {
    private Service service;

    public ServiceJoinEvent(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }
}
