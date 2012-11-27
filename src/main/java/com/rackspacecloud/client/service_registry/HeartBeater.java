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
import java.lang.String;

public class HeartBeater extends BaseClient {
    private static final long INTERVAL_PROACTIVE_MILLIS = 4000L;
    
    private final String sessionId;
    private final Integer heartbeatTimeoutSecs;
    private final long heartbeatIntervalMillis;
    private String nextToken;
    private boolean stopped = false;
    private volatile Thread hbThread = null;
    
    private static final Logger logger = Logger.getLogger(HeartBeater.class);

    public HeartBeater(AuthClient authClient, String sessionId, String initialToken, int timeout) {
        super(authClient);
        this.sessionId = sessionId;
        this.nextToken = initialToken;
        this.heartbeatTimeoutSecs = timeout;
        this.heartbeatIntervalMillis = (long)(timeout * 1000L * (timeout < 15 ? 0.6d : 0.9d));
    }

    private void runInThread() {
        ClientResponse response;
        String path = String.format("/sessions/%s/heartbeat", this.sessionId);

        while (!this.stopped && (this.nextToken != null)) {
            long start = System.currentTimeMillis();
            logger.debug(String.format("Sending hearbeat (timeout=%d secs)...", this.heartbeatTimeoutSecs));

            HeartbeatToken payload = new HeartbeatToken(this.nextToken);

            try {
                response = this.performRequestWithPayload(path, null, new HttpPost(), payload, true, HeartbeatToken.class);
            }
            catch (Exception ex) {
                logger.error(String.format("Got exception while sending heartbeat, stopping heartbeating..."), ex);
                this.stopped = true;
                break;
            }

            this.nextToken = ((HeartbeatToken)response.getBody()).getToken();

            // actual sleep interval is interval - time wasted - INTERVAL_PROACTIVE_MILLIS
            long actualInterval = heartbeatIntervalMillis - System.currentTimeMillis() + start - INTERVAL_PROACTIVE_MILLIS;

            try {
                logger.debug(String.format("Sleeping before sending next heartbeat (delay=%sms, nextToken=%s)", actualInterval, this.nextToken));
                hbThread.sleep(actualInterval);
            }
            catch (InterruptedException ex) {
                logger.debug("Heartbeater woken up. Stopping?");
                continue;
            }
        }
        hbThread = null; // reset sentinel for start().
    }
    
    public synchronized void start() {
        if (hbThread == null) {
            stopped = false;
            hbThread = new Thread(this.toString()) {
                public void run() {
                    runInThread();
                }
            };
            hbThread.start();
        }
    }

    public synchronized  void stop() {
        this.stopped = true;
        hbThread.interrupt();
    }
}
