package com.rackspacecloud.client.service_registry.curator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    public static Collection<Field> getMetaFields(Class cls) {
        List<Field> allFields = new ArrayList<Field>();
        List<Field> metaFields = new ArrayList<Field>();
        for (Field f : cls.getDeclaredFields())
            allFields.add(f);
        for (Field f : cls.getFields())
            allFields.add(f);
        for (Field f : allFields) {
            for (Annotation a : f.getAnnotations()) {
                if (a.annotationType().equals(Meta.class)) {
                    metaFields.add(f);
                }
            }
        }
        return metaFields;  
    }
}
