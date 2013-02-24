package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceTimeoutEvent extends BaseEvent {
    private Service service;

    public ServiceTimeoutEvent(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String toString() {
        return String.format("[ServiceTimeoutEvent id=%s]", this.service.getId());
    }
}
