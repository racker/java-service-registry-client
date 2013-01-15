package com.rackspacecloud.client.service_registry.events;

import java.util.EventListener;

public abstract interface ClientEventListener extends EventListener {
    public void onEvent(ClientEvent ev);
}
