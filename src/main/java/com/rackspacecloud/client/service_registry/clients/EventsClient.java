/**
 *  Copyright 2012 Rackspace
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.rackspacecloud.client.service_registry.clients;

import com.google.gson.reflect.TypeToken;
import com.rackspacecloud.client.service_registry.events.server.*;
import com.rackspacecloud.client.service_registry.objects.*;
import org.apache.http.client.methods.HttpGet;
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.containers.EventsContainer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EventsClient extends BaseClient {
    private static final List VALID_EVENT_TYPES = new ArrayList<String>(Arrays.asList(new String[]{
            "service.join",
            "services.timeout",
            "configuration_value.update",
            "configuration_value.remove"}));

    public EventsClient(AuthClient authClient) {
        super(authClient);
    }

    public List<BaseEvent> list(Map<String, String> options) throws Exception {
        String url = "/events";
        Type type = new TypeToken<EventsContainer>() {}.getType();
        ClientResponse response = this.performRequest(url, null, new HttpGet(), true, type);

        EventsContainer container = (EventsContainer)response.getBody();
        List<BaseEvent> events = this.parseEvents(container.getValues());
        return events;
    }

    /**
     * Takes a list of raw Event objects and convert into a List of BaseEvent
     * objects.
     * @param events List of Event objects.
     * @return List of BaseEvent objects.
     */
    private List<BaseEvent> parseEvents(List<Event> events) throws Exception {
        String type;
        List<BaseEvent> result = new ArrayList<BaseEvent>();

        EventPayload payload;
        String configurationValueId;
        ConfigurationValue oldValue, newValue;

        BaseEvent event;

        for (Event rawEvent : events) {
            type = rawEvent.getType();
            payload = rawEvent.getPayload();

            event = null;

            if (!VALID_EVENT_TYPES.contains(type)) {
                throw new Exception("Unrecognized event type: " + type);
            }

            if (type.compareTo("service.join") == 0) {
                ServiceJoinEventPayload eventPayload = ((ServiceJoinEventPayload)payload);

                event = new ServiceJoinEvent(eventPayload.getService());
                result.add(event);
            }
            else if (type.compareTo("services.timeout") == 0) {
                ServicesTimeoutEventPayload eventPayload = ((ServicesTimeoutEventPayload)payload);

                for (Service service : eventPayload.getServices())  {
                    event = new ServiceTimeoutEvent(service);
                    result.add(event);
                }
            }
            else if (type.compareTo("configuration_value.update") == 0) {
                ConfigurationValueUpdatedEventPayload eventPayload = ((ConfigurationValueUpdatedEventPayload)payload);

                configurationValueId = eventPayload.getConfigurationId();
                oldValue = (eventPayload.getOldValue() == null) ? null : new ConfigurationValue(configurationValueId, eventPayload.getOldValue());
                newValue = new ConfigurationValue(configurationValueId,eventPayload.getNewValue());

                event = new ConfigurationValueUpdatedEvent(oldValue, newValue);
                result.add(event);
            }
            else if (type.compareTo("configuration_value.remove") == 0) {

                ConfigurationValueRemovedEventPayload eventPayload = ((ConfigurationValueRemovedEventPayload)payload);

                configurationValueId = eventPayload.getConfigurationId();
                oldValue = (eventPayload.getOldValue() == null) ? null : new ConfigurationValue(configurationValueId, eventPayload.getOldValue());

                event = new ConfigurationValueRemovedEvent(oldValue);
                result.add(event);
            }
        }

        return result;
    }
}