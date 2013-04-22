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

import java.lang.reflect.Type;

public class Event implements
        InstanceCreator<Event>,
        JsonDeserializer<Event>,
        HasId {
    private String id;
    private Long timestamp;
    private EventType type;
    private EventPayload payload;

    public Event() {}

    public Event(String id, Long timestamp, EventType type, EventPayload payload) {
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

    public EventType getType() {
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
        EventType eventType = EventType.fromString(jsonObject.get("type").getAsString());
        if (eventType == null) {
            throw new Error("Unrecognized event type: " + jsonObject.get("type").getAsString());
        }
        
        JsonObject payloadJsonObject = jsonObject.get("payload").getAsJsonObject();
        Long timestamp = jsonObject.get("timestamp").getAsLong();
        EventPayload payload = null;
        
        switch (eventType) {
            case CONFIGURATION_VALUE_REMOVED:
            case CONFIGURATION_VALUE_UPATED:
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
                break;
            case SERVICE_JOINED:
                payload = new ServiceJoinEventPayload((Service)new Gson().fromJson(jsonObject, Service.TYPE));
                break;
            case SERVICE_REMOVED:
                payload = new ServiceRemoveEventPayload((Service)new Gson().fromJson(jsonObject, Service.TYPE));
                break;
            case SERVICE_TIMEOUT:
                payload = new ServiceTimeoutEventPayload((Service)new Gson().fromJson(jsonObject, Service.TYPE));
                break;
            default:
                throw new Error("Unrecognized event type: " + jsonObject.get("type").getAsString());
        }
        
        return new Event(id, timestamp, eventType, payload);
    }

    @Override
    public String toString() {
        return String.format("[Event id=%s, type=%s, timestamp=%s]", this.getId(), this.getType(), this.getTimestamp());
    }
}