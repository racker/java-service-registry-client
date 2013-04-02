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

package com.rackspacecloud.client.service_registry;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String getIdFromLocationHeader(String value) {
        String[] split = value.split("/");
        return split[split.length - 1];
    }

    public static Integer getNameIndex(List<NameValuePair> array, String name) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getName().equals(name)) {
                return i;
            }
        }

        return null;
    }
}
