package com.rackspacecloud.client.service_registry.events.server;

public class BaseEvent {
    private String id;
    private Long timestamp;

    public BaseEvent(String id, Long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public BaseEvent() {}

    public String getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
