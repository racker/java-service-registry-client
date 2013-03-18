package com.rackspacecloud.client.service_registry.examples;

import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceInstanceBuilder;
import com.netflix.curator.x.discovery.ServiceType;
import com.netflix.curator.x.discovery.UriSpec;
import com.rackspacecloud.client.service_registry.curator.Meta;
import com.rackspacecloud.client.service_registry.objects.Service;

public class FooCuratorService {
    @Meta public String name;
    @Meta public String d;
    @Meta
    public int e;

    public FooCuratorService(String name, String d, int e) {
        this.name = name;
        this.d = d;
        this.e = e;
    }
    
    public static ServiceInstance<FooCuratorService> convert(FooCuratorService foo) throws Exception {
        ServiceInstanceBuilder<FooCuratorService> builder = ServiceInstance.builder();
        // these values are for the most part nonsensical.
        return builder.payload(foo)
            .uriSpec(new UriSpec("http://"))
            .sslPort(2100)
            .serviceType(ServiceType.DYNAMIC)
            .registrationTimeUTC(System.currentTimeMillis())
            .port(2200)
            .address("127.0.0.1")
            .id(String.format("foo-%s-%d", foo.d, foo.e))
            .name(foo.name)
            .build();
    }
     
    public static ServiceInstance<FooCuratorService> convert(Service svc) throws Exception {
        FooCuratorService foo = new FooCuratorService(svc.getMetadata().get("name"),
                                        svc.getMetadata().get("d"), 
                                        Integer.parseInt(svc.getMetadata().get("e")));
        return convert(foo);
    }
}
