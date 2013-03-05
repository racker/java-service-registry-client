package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceJoinEvent extends AbstractServiceEvent {

    public ServiceJoinEvent(Service service) {
        super(service);
    }
    @Override
    public String toString() {
        return String.format("[ServiceJoinEvent id=%s]", this.getService().getId());
    }
}
