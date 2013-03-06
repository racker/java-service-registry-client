package com.rackspacecloud.client.service_registry.curator;

class Utils {
    public static final int MAX_TAG_LENGTH = 55;
    
    // 1..55 chars. anything goes.
    public static String sanitizeTag(String s) {
        // if > 55 chars, assume max entropy is at the end (like a class name).
        if (s.length() > MAX_TAG_LENGTH) {
            s = s.substring(s.length() - MAX_TAG_LENGTH);
        }
        return s;
    }
}
