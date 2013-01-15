package com.rackspacecloud.client.service_registry.events;

import com.rackspacecloud.client.service_registry.HeartBeater;

public class HeartbeatStoppedEvent extends ClientEvent {
    private final Throwable err;
    
    public HeartbeatStoppedEvent(HeartBeater hb, int status) {
        this(hb, null, status);
    }
    
    public HeartbeatStoppedEvent(HeartBeater hb, Throwable err, int status) {
        super(hb, status);
        this.err = err;
    }
    
    public boolean isError() { 
        return err != null || getHttpStatus() >= 400;
    }
    
    public Throwable getError() { return err; }
}
