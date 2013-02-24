package com.rackspacecloud.client.service_registry.events.server;

import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;

public class ConfigurationValueUpdatedEvent extends BaseEvent {
    private ConfigurationValue oldValue;
    private ConfigurationValue newValue;

    public ConfigurationValueUpdatedEvent(ConfigurationValue oldValue, ConfigurationValue newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ConfigurationValue getOldValue() {
        return oldValue;
    }

    public ConfigurationValue getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        String id, oldValue, newValue;

        id = this.newValue.getId();
        oldValue = (this.oldValue == null) ? "null" : this.oldValue.getValue();
        newValue = this.newValue.getValue();

        return String.format("[ConfigurationValueUpdatedEvent id=%s, old_value=%s, new_value=%s]",
                             id, oldValue, newValue);
    }
}
