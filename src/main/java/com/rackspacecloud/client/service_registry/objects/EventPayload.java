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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventPayload implements
        InstanceCreator<EventPayload>,
        JsonDeserializer<EventPayload> {

    @Override
    public EventPayload createInstance(Type type) {
        return this;
    }

    @Override
    public EventPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        // TODO: This is nasty - propagate type here
        if (json.getClass().equals(JsonObject.class)) {
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("configuration_value_id")) {
                // configuration_value.update, configuration_value.remove
                String id = jsonObject.get("configuration_value_id").getAsString();
                String oldValue, newValue;

                if (jsonObject.get("old_value").isJsonNull()) {
                    oldValue = null;
                }
                else {
                    oldValue = jsonObject.get("old_value").getAsString();
                }

                if (jsonObject.has("new_value")) {
                    newValue = jsonObject.get("new_value").getAsString();

                    return new ConfigurationValueUpdatedEventPayload(id, oldValue, newValue);
                }
                else {
                    return new ConfigurationValueRemovedEventPayload(id, oldValue);
                }
            }
            else {
                // service.join
                Service service = new Gson().fromJson(jsonObject, new TypeToken<Service>() {}.getType());
                return new ServiceJoinEventPayload(service);
            }
        }
        else if (json.getClass().equals(JsonArray.class)) {
            // services.timeout
            JsonArray jsonArray = json.getAsJsonArray();
            ArrayList<Service> services = new Gson().fromJson(jsonArray, new TypeToken<ArrayList<Service>>() {}.getType());

            return new ServicesTimeoutEventPayload(services);
        }

        return null;
    }
}
