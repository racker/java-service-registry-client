package com.rackspacecloud.client.service_registry.examples;

import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.rackspacecloud.client.service_registry.curator.RSRServiceDiscoveryBuilder;

public class CuratorDeploy {
    public static void main(String args[]) {
        try {
            // Deploy two classes of service: instances of FooCuratorService and instances of BarCuratorService.
            
            // remember that Curator discovery facets services on name (ids or "instance ids" are still unique).
            FooCuratorService foo0 = new FooCuratorService("name1", "one", 1);
            FooCuratorService foo1 = new FooCuratorService("name1", "two", 2);
            FooCuratorService foo3 = new FooCuratorService("name2", "one", 11);
            FooCuratorService foo4 = new FooCuratorService("name2", "three", 333);
            FooCuratorService foo5 = new FooCuratorService("name2", "four", 4444);
            
            BarCuratorService bar0 = new BarCuratorService("name1", "three", 3, 33.34f);
            BarCuratorService bar1 = new BarCuratorService("name1", "four", 4, 44.45f);
            
            ServiceDiscovery<FooCuratorService> fooDiscovery = RSRServiceDiscoveryBuilder.builder(FooCuratorService.class)
                .withUser(System.getProperty("user"))
                .withApiKey(System.getProperty("apiKey"))
                .withApiUrl(System.getProperty("apiUrl"))
                .build();
            fooDiscovery.start();
            
            fooDiscovery.registerService(FooCuratorService.convert(foo0));
            fooDiscovery.registerService(FooCuratorService.convert(foo1));
            fooDiscovery.registerService(FooCuratorService.convert(foo3));
            fooDiscovery.registerService(FooCuratorService.convert(foo4));
            fooDiscovery.registerService(FooCuratorService.convert(foo5));
            
            ServiceDiscovery<BarCuratorService> barDiscovery = RSRServiceDiscoveryBuilder.builder(BarCuratorService.class)
                .withUser(System.getProperty("user"))
                .withApiKey(System.getProperty("apiKey"))
                .withApiUrl(System.getProperty("apiUrl"))
                .build();
            barDiscovery.start();
            
            barDiscovery.registerService(BarCuratorService.convert(bar0));
            barDiscovery.registerService(BarCuratorService.convert(bar1));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
