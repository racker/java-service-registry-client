package com.rackspacecloud.client.service_registry.curator;

import com.netflix.curator.x.discovery.ServiceInstance;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.HeartBeater;
import com.rackspacecloud.client.service_registry.ServiceCreateResponse;
import com.rackspacecloud.client.service_registry.events.client.ClientEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatAckEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatErrorEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatEventListener;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatStoppedEvent;
import com.rackspacecloud.client.service_registry.objects.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for managing the lifecycle of an RSR service.
 * todo: Provide a lifecycle strategy. this will be responsible for deciding how/if/when to register and deal with 
 * various events.
 */
class ServiceTracker<T> {
    public static final String DISCOVERY = "discovery";
    public static final String CURATOR_TAG = "curator-x-discovery";
    public static final String NAME = "name";
    
    private static final String ADDRESS = "address";
    private static final String PORT = "port";
    private static final String REG_TIME = "regtime";
    private static final String SVC_TYPE = "svcType";
    private static final String SSL_PORT = "sslPort";
    private static final String URI_SPEC = "uriSpec";
    
    private volatile Service service = null;
    private volatile HeartBeater heartbeater = null;
    
    private final String typeTag;
    private final ServiceInstance<T> instance;
    private final Client client;
    
    private final HeartbeatEventListener heartbeatListener;
    
    public ServiceTracker(Client client, ServiceInstance<T> instance, String typeTag) {
        this.client = client;
        this.instance = instance;
        this.typeTag = typeTag;
        this.heartbeatListener = new CuratorHeartbeatEventListener();
    }
    
    public synchronized Service register() throws Exception {
        if (this.service == null) {
            List<String> tags = new ArrayList<String>();
            tags.add(typeTag);
            tags.add(this.instance.getName());
            tags.add(CURATOR_TAG);
            ServiceCreateResponse res = client.getServicesClient().create(
                this.instance.getId(), 
                30, 
                tags, 
                getMetadata(this.instance, this.typeTag));
            heartbeater = res.getHeartbeater();
            heartbeater.addEventListener(this.heartbeatListener);
            heartbeater.start();
            this.service = res.getService();
        }
        return service;
    }
    
    public synchronized void stop() {
        if (this.heartbeater != null) {
            this.heartbeater.stop();
            // heartbeat event eventually causes everything to get cleaned up.
        }
    }
    
    private static Map<String, String> getMetadata(ServiceInstance service, String typeTag) {
        Map<String, String> map = new HashMap<String, String>();
        
        map.put(DISCOVERY, typeTag);
        map.put(NAME, service.getName());
        map.put(ADDRESS, service.getAddress());
        if (service.getPort() != null)
            map.put(PORT, service.getPort().toString());
        map.put(REG_TIME, Long.toString(service.getRegistrationTimeUTC()));
        map.put(SVC_TYPE, service.getServiceType().name());
        if (service.getSslPort() != null)
            map.put(SSL_PORT, service.getSslPort().toString());
        if (service.getUriSpec() != null)
            map.put(URI_SPEC, service.getUriSpec().build());
        
        // what else?
        for (Field f : Utils.getMetaFields(service.getPayload().getClass())) {
            try {
                f.setAccessible(true);
                map.put(f.getName(), f.get(service.getPayload()).toString());
            } catch (Exception ex) {
                // todo: log
            }
        }
        
        return map;
    }
    
    
    private class CuratorHeartbeatEventListener extends HeartbeatEventListener {   
        
        private void resetService(ClientEvent event) {
            ((HeartBeater)event.getSource()).removeEventListener(this);
            ServiceTracker.this.service = null;
            try {
                // make sure the thread is stopped.
                ServiceTracker.this.heartbeater.stop();
            } catch (Exception ignore) {
                // todo: log maybe?
            }
            ServiceTracker.this.heartbeater = null;
        }
        
        @Override
        public void onAck(HeartbeatAckEvent ack) {
            // do nothing.
        }
 
        @Override
        public void onStopped(HeartbeatStoppedEvent stopped) {
            // session was stopped cleanly.
            resetService(stopped);
            if (stopped.isError()) {
                try {
                    register();
                } catch (Exception ex) {
                    // depends on what the exception policy is.
                }
            }
        }
 
        @Override
        public void onError(HeartbeatErrorEvent error) {
            resetService(error);
            if (error.isError()) {
                try {
                    register();
                } catch (Exception ex) {
                    // depends on what the exception policy is.
                }
            }
            
        }
    }
}
