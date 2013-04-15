package com.rackspacecloud.client.service_registry.objects;

public enum EventType {
    SERVICE_JOINED("service.join"),
    SERVICE_REMOVED("service.remove"),
    SERVICE_TIMEOUT("service.timeout"),
    CONFIGURATION_VALUE_REMOVED("configuration_value.remove"),
    CONFIGURATION_VALUE_UPATED("configuration_value.update");

    private final String str;
    
    private EventType(String str) {
        this.str = str;
    }
    
    @Override
    public String toString() {
        return this.str;
    }

    public static EventType fromString(String str) {
        if (SERVICE_JOINED.str.equals(str)) 
            return SERVICE_JOINED;
        else if (SERVICE_REMOVED.str.equals(str))
            return SERVICE_REMOVED;
        else if (SERVICE_TIMEOUT.str.equals(str))
            return SERVICE_TIMEOUT;
        else if (CONFIGURATION_VALUE_UPATED.str.equals(str))
            return CONFIGURATION_VALUE_UPATED;
        else if (CONFIGURATION_VALUE_REMOVED.str.equals(str))
            return CONFIGURATION_VALUE_REMOVED;
        else
            return null; // instead of throwing an exception.
    }

}
