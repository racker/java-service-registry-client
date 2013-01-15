package com.rackspacecloud.client.service_registry.events;

import java.util.EventObject;

public class ClientEvent extends EventObject 
{
    public ClientEvent(Object source) {
        super(source);
    }
}
