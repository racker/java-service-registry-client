package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.Service;

public class ServiceTimeoutEvent extends AbstractServiceEvent {

    public ServiceTimeoutEvent(Service service) {
        super(service);
    }

    @Override
    public String toString() {
        return String.format("[ServiceTimeoutEvent id=%s]", this.getService().getId());
    }
}
