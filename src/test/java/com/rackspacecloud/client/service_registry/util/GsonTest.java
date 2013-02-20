package com.rackspacecloud.client.service_registry.util;

import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.junit.Test;

public class GsonTest {
    
    @Test
    public void testGsonCanSetPrivateFields() {
        String json = "{\"privateInt\": 42, \"privateString\": \"foo\"}";
        TestClass instance = new GsonBuilder().create().fromJson(json, TestClass.class);
        Assert.assertEquals(42, instance.privateInt);
        Assert.assertEquals("foo", instance.privateString);
    }
    
    public static class TestClass {
        private int privateInt;
        private String privateString;
    }
}
