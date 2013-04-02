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

package com.rackspacecloud.client.service_registry.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Event implements
        InstanceCreator<Event>,
        JsonDeserializer<Event> {
    private String id;
    private Long timestamp;
    private String type;
    private EventPayload payload;

    public Event() {}

    public Event(String id, Long timestamp, String type, EventPayload payload) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public EventPayload getPayload() {
        return payload;
    }

    @Override
    public Event createInstance(Type type) {
        return this;
    }

    @Override
    public Event deserialize(JsonElement json, Type typeOfT,
                             JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();

        String id = jsonObject.get("id").getAsString();
        String eventType = jsonObject.get("type").getAsString();
        JsonObject payloadJsonObject = jsonObject.get("payload").getAsJsonObject();
        Long timestamp = jsonObject.get("timestamp").getAsLong();

        EventPayload payload = null;

        if (eventType.equals("configuration_value.update") ||
            eventType.equals("configuration_value.remove")) {
            String configurationId =  payloadJsonObject.get("configuration_value_id").getAsString();
            String oldValue, newValue;

            if (payloadJsonObject.get("old_value").isJsonNull()) {
                oldValue = null;
            }
            else {
                oldValue = payloadJsonObject.get("old_value").getAsString();
            }

            if (payloadJsonObject.has("new_value")) {
                newValue = payloadJsonObject.get("new_value").getAsString();

                payload =  new ConfigurationValueUpdatedEventPayload(configurationId, oldValue, newValue);
            }
            else {
                payload =  new ConfigurationValueRemovedEventPayload(configurationId, oldValue);
            }
        }
        else if (eventType.equals("service.join") || eventType.equals("service.timeout") ||
                 eventType.equals("service.remove")) {
            Service service = new Gson().fromJson(jsonObject,
                                                  new TypeToken<Service>() {}.getType());

            if (eventType.equals("service.join")) {
                payload = new ServiceJoinEventPayload(service);
            }
            else if (eventType.equals("service.timeout")) {
                payload = new ServiceTimeoutEventPayload(service);
            }
            else if (eventType.equals("service.remove")) {
                payload = new ServiceRemoveEventPayload(service);
            }
        }
        else {
            throw new Error("Unrecognized event type: " + eventType);
        }

        return new Event(id, timestamp, eventType, payload);
    }

    @Override
    public String toString() {
        return String.format("[Event id=%s, type=%s, timestamp=%s]", this.getId(),
                             this.getType(), this.getTimestamp());
    }
}