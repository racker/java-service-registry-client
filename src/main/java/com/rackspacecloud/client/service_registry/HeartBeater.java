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

import com.rackspacecloud.client.service_registry.clients.AuthClient;
import com.rackspacecloud.client.service_registry.clients.BaseClient;
import com.rackspacecloud.client.service_registry.objects.HeartbeatToken;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;

import java.lang.Exception;
import java.lang.Integer;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.String;

public class HeartBeater extends BaseClient implements Runnable {
    private String sessionId;
    private Integer heartbeatTimeout;
    private double heartbeatInterval;
    private String nextToken;
    private boolean stopped = false;

    private static Logger logger = Logger.getLogger(HeartBeater.class);

    public HeartBeater(AuthClient authClient, String sessionId, String initialToken, Integer timeout) {
        super(authClient);

        this.sessionId = sessionId;
        this.nextToken = initialToken;
        this.heartbeatTimeout = timeout;

        if (this.heartbeatTimeout < 15) {
            this.heartbeatInterval = (this.heartbeatTimeout * 0.6);
        }
        else {
            this.heartbeatInterval = (this.heartbeatTimeout * 0.9);
        }
    }

    public void run() {
        ClientResponse response;
        String path = String.format("/sessions/%s/heartbeat", this.sessionId);

        while (!this.stopped && (this.nextToken != null)) {
            logger.debug(String.format("Sending hearbeat (timeout=%d)...", this.heartbeatTimeout));

            HeartbeatToken payload = new HeartbeatToken(this.nextToken);

            try {
                response = this.performRequestWithPayload(path, null, new HttpPost(), payload, true, HeartbeatToken.class);
            }
            catch (Exception ex) {
                logger.error(String.format("Got exception while sending heartbeat, stopping heartbeating..."), ex);
                this.stopped = true;
                return;
            }

            this.nextToken = ((HeartbeatToken)response.getBody()).getToken();

            double interval = this.heartbeatInterval * 1000;

            try {
                logger.debug(String.format("Sleeping before sending next heartbeat (delay=%sms, nextToken=%s)", interval, this.nextToken));
                java.lang.Thread.sleep((long) interval);
            }
            catch (InterruptedException ex) {
                logger.error("Error while sleeping", ex);
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
