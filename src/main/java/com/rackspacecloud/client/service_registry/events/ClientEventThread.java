package com.rackspacecloud.client.service_registry.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientEventThread {
    private static final ExecutorService workers = Executors.newSingleThreadExecutor();
    
    public static Future submit(Runnable r) {
        return workers.submit(r);
    }
}
