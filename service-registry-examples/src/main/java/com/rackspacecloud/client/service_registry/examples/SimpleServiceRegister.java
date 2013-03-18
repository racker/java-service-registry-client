package com.rackspacecloud.client.service_registry.examples;

import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.HeartBeater;
import com.rackspacecloud.client.service_registry.Region;
import com.rackspacecloud.client.service_registry.ServiceCreateResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleServiceRegister {
    public static void main(String args[]) {
        Client c = new Client(
            System.getProperty("user"),
            System.getProperty("apiKey"),
            Region.US,
            System.getProperty("apiUrl")
        );
        List<String> tags = new ArrayList<String>();
        Map<String, String> metadata = new HashMap<String, String>();
        
        try {
            ServiceCreateResponse r = c.getServicesClient().create("gary-test-service-staging-0", 20, tags, metadata);
            HeartBeater hb = r.getHeartbeater();
            hb.start();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}
