package com.rackspacecloud.client.service_registry.events;

import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.HeartBeater;

public class HeartbeatAckEvent extends ClientEvent {
    private final ClientResponse response;
    
    public HeartbeatAckEvent(HeartBeater hb, ClientResponse response) {
        super(hb);
        this.response = response;
    }
    
    public ClientResponse getResponse() { return response; }
}
