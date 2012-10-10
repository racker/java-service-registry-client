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

package com.rackspacecloud.client.service_registry.auth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Token {
    protected String id;
    protected String expires;
    protected HashMap<String, Integer> tenant;

    public String getId() {
        return id;
    }

    public Long getExpires() {
        Date date = null;
        SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");

        try {
            date = ISO8601DATEFORMAT.parse(expires.replaceAll("\\-0([0-9]){1}\\:00", "-0$100"));
        }
        catch (ParseException ex) {
            return null;
        }

        return date.getTime();
    }

    public HashMap<String, Integer> getTenant() {
        return tenant;
    }
}
