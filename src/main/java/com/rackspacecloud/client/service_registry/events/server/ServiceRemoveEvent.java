package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceRemoveEvent extends BaseEvent {
    private Service service;

    public ServiceRemoveEvent(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String toString() {
        return String.format("[ServiceRemoveEvent id=%s]", this.service.getId());
    }
}
