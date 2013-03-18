package com.rackspacecloud.client.service_registry.events.client;

import java.util.EventObject;

public class ClientEvent extends EventObject {
    private final int httpStatus;
    
    public ClientEvent(Object source, int httpStatus) {
        super(source);
        this.httpStatus = httpStatus;
    }
    
    public int getHttpStatus() { return httpStatus; }
}
