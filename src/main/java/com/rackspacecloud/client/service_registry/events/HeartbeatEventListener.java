package com.rackspacecloud.client.service_registry.events;


public abstract class HeartbeatEventListener implements ClientEventListener {
    public abstract void onAck(HeartbeatAckEvent ack);
    public abstract void onStopped(HeartbeatStoppedEvent stopped);
    public abstract void onError(HeartbeatErrorEvent error);

    public void onEvent(ClientEvent ev) {
        if (ev instanceof HeartbeatAckEvent) {
            onAck((HeartbeatAckEvent)ev);
        } else if (ev instanceof HeartbeatStoppedEvent) {
            onStopped((HeartbeatStoppedEvent)ev);
        } else if (ev instanceof HeartbeatErrorEvent) {
            onError((HeartbeatErrorEvent)ev);
        }
    }
}
