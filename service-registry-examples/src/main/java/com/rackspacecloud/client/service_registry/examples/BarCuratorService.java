package com.rackspacecloud.client.service_registry.examples;

import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceInstanceBuilder;
import com.netflix.curator.x.discovery.ServiceType;
import com.netflix.curator.x.discovery.UriSpec;
import com.rackspacecloud.client.service_registry.curator.Meta;
import com.rackspacecloud.client.service_registry.objects.Service;

public class BarCuratorService {
    // all fields annotated with @Meta will be seen as metadata in the RSR service instance.
    @Meta public String name;
    @Meta public String a;
    @Meta public int b;
    @Meta public float c;
    
    // this constructor gets used by one of the convert methods (after a service is read from RSR).
    public BarCuratorService(String name, String a, int b, float c) {
        this.name = name;
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    // one of the required convert methods.  This one converts instances of the users service class into
    // ServiceInstance<T> instances required by the Curator interfaces.
    public static ServiceInstance<BarCuratorService> convert(BarCuratorService bar) throws Exception {
        ServiceInstanceBuilder<BarCuratorService> builder = ServiceInstance.builder();
        // these values are for the most part nonsensical.
        return builder.payload(bar)
            .uriSpec(new UriSpec("http://"))
            .sslPort(2400)
            .serviceType(ServiceType.STATIC)
            .port(2300)
            .address("127.0.0.1")
            .id(String.format("bar-%s-%d-%s", bar.a, bar.b, bar.c))
            .name(bar.name)
            .build();
    }
    
    // the other required convert method.  This one converts RSR service instances (which are very generic) into 
    // ServiceInstance<T> instances required by the Curator interfaces.
    public static ServiceInstance<BarCuratorService> convert(Service svc) throws Exception {
        BarCuratorService bar = new BarCuratorService(svc.getMetadata().get("name"),
                                        svc.getMetadata().get("a"), 
                                        Integer.parseInt(svc.getMetadata().get("b")), 
                                        Float.parseFloat(svc.getMetadata().get("c")));
        return convert(bar);
    }
}
