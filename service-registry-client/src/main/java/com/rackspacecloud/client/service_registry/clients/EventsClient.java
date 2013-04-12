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
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.events.server.*;
import com.rackspacecloud.client.service_registry.objects.*;
import org.apache.http.client.methods.HttpGet;
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.containers.EventsContainer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventsClient extends BaseClient {
    private static final List VALID_EVENT_TYPES = new ArrayList<String>(Arrays.asList(new String[]{
            "service.join",
            "service.timeout",
            "service.remove",
            "configuration_value.update",
            "configuration_value.remove"}));

    public EventsClient(AuthClient authClient, String apiUrl) {
        super(authClient, apiUrl);
    }

    public Iterator<BaseEvent> list(PaginationOptions paginationOptions) throws Exception {
        Type type = new TypeToken<EventsContainer>() {}.getType();
        
        final Iterator<Event> eventIterator = this.getListIterator(Event.class,
                                                             "/events",
                                                             paginationOptions,
                                                             new HashMap<String, String>(),
                                                             new HttpGet(),
                                                             true,
                                                             type);
        return new Iterator<BaseEvent>() {
            public boolean hasNext() {
                return eventIterator.hasNext();
            }

            public BaseEvent next() {
                try {
                    return parseEvent(eventIterator.next());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            public void remove() {
                eventIterator.remove();
            }
        };
        
        
    }

    /**
     * Converts Event to BaseEvent.
     * @param rawEvent Event object.
     * @return BaseEvent object.
     */
    private BaseEvent parseEvent(Event rawEvent) throws Exception {
        if (!VALID_EVENT_TYPES.contains(rawEvent.getType())) {
            throw new Exception("Unrecognized event type: " + rawEvent.getType());
        }

        if (rawEvent.getType().compareTo("service.join") == 0) {
            ServiceJoinEventPayload eventPayload = ((ServiceJoinEventPayload)rawEvent.getPayload());

            return new ServiceJoinEvent(eventPayload.getService());
        }
        else if (rawEvent.getType().compareTo("service.timeout") == 0) {
            ServiceTimeoutEventPayload eventPayload = ((ServiceTimeoutEventPayload)rawEvent.getPayload());

            return new ServiceTimeoutEvent(eventPayload.getService());
        }
        else if (rawEvent.getType().compareTo("service.remove") == 0) {
            ServiceRemoveEventPayload eventPayload = ((ServiceRemoveEventPayload)rawEvent.getPayload());

            return new ServiceRemoveEvent(eventPayload.getService());
        }
        else if (rawEvent.getType().compareTo("configuration_value.update") == 0) {
            ConfigurationValueUpdatedEventPayload eventPayload = ((ConfigurationValueUpdatedEventPayload)rawEvent.getPayload());
            String configurationValueId = eventPayload.getConfigurationId();
            ConfigurationValue oldValue = (eventPayload.getOldValue() == null) ? null : new ConfigurationValue(configurationValueId, eventPayload.getOldValue());
            ConfigurationValue newValue = new ConfigurationValue(configurationValueId,eventPayload.getNewValue());

            return new ConfigurationValueUpdatedEvent(oldValue, newValue);
        }
        else if (rawEvent.getType().compareTo("configuration_value.remove") == 0) {
            ConfigurationValueRemovedEventPayload eventPayload = ((ConfigurationValueRemovedEventPayload)rawEvent.getPayload());
            String configurationValueId = eventPayload.getConfigurationId();
            ConfigurationValue oldValue = (eventPayload.getOldValue() == null) ? null : new ConfigurationValue(configurationValueId, eventPayload.getOldValue());

            return new ConfigurationValueRemovedEvent(oldValue);
        }
        else {
            throw new Exception("Unexpected even type: " + rawEvent.getType());
        }
    }
}