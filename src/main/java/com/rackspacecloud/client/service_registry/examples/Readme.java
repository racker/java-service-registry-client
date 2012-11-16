package com.rackspacecloud.client.service_registry.examples;

import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.Region;
import com.rackspacecloud.client.service_registry.SessionCreateResponse;
import com.rackspacecloud.client.service_registry.tools.Example;

import java.util.HashMap;
import java.util.Map;

public class Readme {
    
    @Example(tag="readme_example_1")
    public void readMeExample1() throws Exception {
        Client client = new Client("MY_RAX_USER", "MY_RAX_API_KEY", Region.US);
        
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("host", "127.0.0.1");
        metadata.put("is_testdata", "absolutely");
        metadata.put("version", "0u812");
    
        SessionCreateResponse sessionResponse = client.sessions.create(15, metadata);
        
        sessionResponse.getHeartbeater().start();
    }
}
