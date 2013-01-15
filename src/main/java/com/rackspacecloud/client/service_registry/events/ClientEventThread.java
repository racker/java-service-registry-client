package com.rackspacecloud.client.service_registry.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This singleton contains the thread that will process and emit client events.  Right now it is limited to one thread
 * to ensure a serial property.
 */
public class ClientEventThread {
    private static final ExecutorService workers = Executors.newSingleThreadExecutor();
    
    public static Future submit(Runnable r) {
        return workers.submit(r);
    }
}
