package com.rackspacecloud.client.service_registry.examples;

import com.google.common.base.Joiner;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceCacheBuilder;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.rackspacecloud.client.service_registry.curator.RSRServiceDiscoveryBuilder;

import java.util.Collection;
import java.util.List;

public class CuratorQuery {
    public static void main(String args[]) {
        try {
            ServiceDiscovery<FooCuratorService> fooDiscovery = RSRServiceDiscoveryBuilder.builder(FooCuratorService.class)
                .withUser(System.getProperty("user"))
                .withApiKey(System.getProperty("apiKey"))
                .withApiUrl(System.getProperty("apiUrl"))
                .build();
            ServiceDiscovery<BarCuratorService> barDiscovery = RSRServiceDiscoveryBuilder.builder(BarCuratorService.class)
                .withUser(System.getProperty("user"))
                .withApiKey(System.getProperty("apiKey"))
                .withApiUrl(System.getProperty("apiUrl"))
                .build();
            
            ServiceInstance<FooCuratorService> foo0si = fooDiscovery.queryForInstance("name1", "foo-three-333");
            FooCuratorService foo0 = foo0si.getPayload();
            
            Collection<String> names = fooDiscovery.queryForNames();
            System.out.println(Joiner.on(",").join(names));
            System.out.println(String.format("There were %d names", names.size()));
            
            // should be 2 name1 foo services, 3 name2 foo services.
            Collection<ServiceInstance<FooCuratorService>> naFooCuratorServices = fooDiscovery.queryForInstances("name1");
            Collection<ServiceInstance<FooCuratorService>> nbFooCuratorServices = fooDiscovery.queryForInstances("name2");
            
            // should be two name1 bar services, 0 name2 bar services.
            Collection<ServiceInstance<BarCuratorService>> naBarCuratorServices = barDiscovery.queryForInstances("name1");
            Collection<ServiceInstance<BarCuratorService>> nbBarCuratorServices = barDiscovery.queryForInstances("name2");
            
            System.out.println(String.format("There were %d (2) name1 services in foo", naFooCuratorServices.size()));
            System.out.println(String.format("There were %d (3) name2 services in foo", nbFooCuratorServices.size()));
            System.out.println(String.format("There were %d (2) name1 services in bar", naBarCuratorServices.size()));
            System.out.println(String.format("There were %d (0) name2 services in bar", nbBarCuratorServices.size()));
            
            // Ok. Let's use the service cache.
            ServiceCacheBuilder<FooCuratorService> fooCacheBuilder = fooDiscovery
                    .serviceCacheBuilder()
                    .name("name1");
            ServiceCache<FooCuratorService> fooCache = fooCacheBuilder.build();
            fooCache.start();
            List<ServiceInstance<FooCuratorService>> instances = fooCache.getInstances();
            System.out.println(String.format("There were %d instances in the cache", instances.size()));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
