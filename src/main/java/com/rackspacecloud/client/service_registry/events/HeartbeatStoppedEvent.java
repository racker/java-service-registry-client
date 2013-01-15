package com.rackspacecloud.client.service_registry.events;

import com.rackspacecloud.client.service_registry.HeartBeater;

public class HeartbeatStoppedEvent extends ClientEvent {
    private final Throwable err;
    
    public HeartbeatStoppedEvent(HeartBeater hb) {
        this(hb, null);
    }
    
    public HeartbeatStoppedEvent(HeartBeater hb, Throwable err) {
        super(hb);
        this.err = err;
    }
    
    public boolean isError() { return err != null; }
    public Throwable getError() { return err; }
}
