package com.rackspacecloud.client.service_registry.objects;

public enum EventType {
    SERVICE_JOINED("service.join".intern()),
    SERVICE_REMOVED("service.remove".intern()),
    SERVICE_TIMEOUT("service.timeout".intern()),
    CONFIGURATION_VALUE_REMOVED("configuration_value.remove".intern()),
    CONFIGURATION_VALUE_UPATED("configuration_value.update".intern());

    private final String str;
    
    private EventType(String str) {
        this.str = str;
    }
    
    @Override
    public String toString() {
        return this.str;
    }

    public static EventType fromString(String str) {
        if (str == SERVICE_JOINED.str) 
            return SERVICE_JOINED;
        else if (str == SERVICE_REMOVED.str)
            return SERVICE_REMOVED;
        else if (str == SERVICE_TIMEOUT.str)
            return SERVICE_TIMEOUT;
        else if (str == CONFIGURATION_VALUE_UPATED.str)
            return CONFIGURATION_VALUE_UPATED;
        else if (str == CONFIGURATION_VALUE_REMOVED.str)
            return CONFIGURATION_VALUE_REMOVED;
        else
            return null; // instead of throwing an exception.
    }

}
