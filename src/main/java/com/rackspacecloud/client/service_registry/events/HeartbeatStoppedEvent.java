package com.rackspacecloud.client.service_registry.events;

import com.rackspacecloud.client.service_registry.HeartBeater;

public class HeartbeatStoppedEvent extends ClientEvent {
    
    public HeartbeatStoppedEvent(HeartBeater hb, int status) {
        super(hb, status);
    }
    
    public boolean isError() { 
        return getHttpStatus() >= 400;
    }
    
    
}
