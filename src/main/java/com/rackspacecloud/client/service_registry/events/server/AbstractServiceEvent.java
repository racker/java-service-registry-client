package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.Service;

public class AbstractServiceEvent extends BaseEvent {
    private final Service service;
    
    public AbstractServiceEvent(Service service) {
        this.service = service;
    }
    
    public Service getService() { return service; }
}
