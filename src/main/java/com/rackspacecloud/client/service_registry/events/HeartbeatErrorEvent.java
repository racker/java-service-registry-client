package com.rackspacecloud.client.service_registry.events;

public class HeartbeatErrorEvent extends ClientEvent {
    
    private final Throwable err;
    
    public HeartbeatErrorEvent(Object source, Throwable err, int httpStatus) {
        super(source, httpStatus);
        this.err = err;
    }
    
    public Throwable getError() { return err; }
    public boolean isError() { return true; }
}
