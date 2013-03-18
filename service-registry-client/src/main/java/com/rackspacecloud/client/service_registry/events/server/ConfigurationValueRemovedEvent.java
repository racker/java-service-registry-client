package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;

public class ConfigurationValueRemovedEvent extends BaseEvent {
    private ConfigurationValue oldValue;

    public ConfigurationValueRemovedEvent(ConfigurationValue oldValue) {
        this.oldValue = oldValue;
    }

    public ConfigurationValue getOldValue() {
        return oldValue;
    }

    @Override
    public String toString() {
        String id, oldValue;

        id = this.oldValue.getId();
        oldValue = this.oldValue.getValue();
        return String.format("[ConfigurationValueRemovedEvent id=%s, old_value=%s",
                             id, oldValue);
    }
}
