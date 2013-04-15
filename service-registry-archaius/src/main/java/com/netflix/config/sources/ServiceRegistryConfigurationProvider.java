package com.netflix.config.sources;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;
import com.rackspacecloud.client.service_registry.events.server.BaseEvent;
import com.rackspacecloud.client.service_registry.events.server.ConfigurationValueRemovedEvent;
import com.rackspacecloud.client.service_registry.events.server.ConfigurationValueUpdatedEvent;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import com.rackspacecloud.client.service_registry.objects.Event;
import com.rackspacecloud.client.service_registry.objects.Service;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

public class ServiceRegistryConfigurationProvider implements PolledConfigurationSource {

    private static Logger logger = LoggerFactory.getLogger(ServiceRegistryConfigurationProvider.class);

    private static final String DELIMITER = ".";
    public static final String PREFIX = "serverset";
    public static final String INTEREST = PREFIX + DELIMITER + "interest";
    private static final String SEPARATOR = ",";
    private static final String SUFFIX = "addresses";

    private Long lastPollTs = null;
    private String lastMarker = null;

    public static final DynamicStringProperty dynamicServiceTags = DynamicPropertyFactory.getInstance().getStringProperty(INTEREST, "");

    private final ServiceRegistryClient client;

    public ServiceRegistryConfigurationProvider(ServiceRegistryClient client) {
        this.client = client;
    }

    @Override
    public PollResult poll(boolean initial, Object checkPoint)
            throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        if (this.lastPollTs == null) {
            // First poll, can't use events feed yet. Just retrieve all the
            // configuration values
            for (ConfigurationValue value : client.getConfiguration()) {
                map.put(value.getId(), value.getValue());
            }
        }
        else {
            List<BaseEvent> events = client.getEvents(this.lastMarker);
            ConfigurationValue oldValue, newValue;

            for (BaseEvent event : events) {
                // TODO: skip first event
                if (event instanceof ConfigurationValueUpdatedEvent) {
                    newValue = ((ConfigurationValueUpdatedEvent) event).getNewValue();
                    String configurationId = newValue.getId();

                    map.put(configurationId, newValue);

                }
                else if (event instanceof ConfigurationValueRemovedEvent) {
                    oldValue = ((ConfigurationValueRemovedEvent) event).getOldValue();
                    String configurationId = oldValue.getId();

                    if (map.containsKey(configurationId)) {
                        map.remove(configurationId);
                    }
                }
            }
        }

        this.lastPollTs = (new Date().getTime() / 1000);

        // Do a query to get all the service tags we're interested in,
        // once we have that we'll namespace the parameters
        String tags = dynamicServiceTags.get();

        if (!tags.isEmpty()) {
            Set<String> serviceTags = new HashSet<String>(Arrays.asList(tags.split(SEPARATOR)));
            for (String tag: serviceTags) {
                Set<InetSocketAddress> pairs = new HashSet<InetSocketAddress>();
                String key = PREFIX + DELIMITER + tag + DELIMITER + SUFFIX;

                for (Service service : client.getServices(tag)) {
                    InetSocketAddress pair = getHostPortPair(service);

                    if (pair != null) {
                        pairs.add(pair);
                    }
                }

                if (!pairs.isEmpty()) {
                    map.put(key, StringUtils.join(pairs, SEPARATOR));
                }
            }
        }
        return PollResult.createFull(map);
    }

    @Override
    public String toString() {
        return "ServiceRegistryConfigurationProvider [client=" + client.toString() + "]";
    }

    /**
     * Safely get the information from our convention
     * @param svc
     * @return
     */
    public static InetSocketAddress getHostPortPair(Service svc) {
        try {
            return new InetSocketAddress(svc.getMetadata().get("service.host"),
                    Integer.parseInt(svc.getMetadata().get("service.port")));
        } catch (Exception e) {
            logger.error("Exception extracting metadata from service instance {}", svc, e);
            return null;
        }
    }

    public static Service setHostPortPair(String host, int port, Service svc) {
        svc.getMetadata().put("service.host", host);
        svc.getMetadata().put("service.port", String.valueOf(port));
        return svc;
    }
}
