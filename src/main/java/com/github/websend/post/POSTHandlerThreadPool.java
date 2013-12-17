package com.github.websend.post;

import com.github.websend.Main;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import org.apache.http.impl.client.HttpClientBuilder;

public class POSTHandlerThreadPool {

    private ArrayList<POSTHandlerThread> busyThreads;
    private ConcurrentLinkedQueue<POSTHandlerThread> availableThreadsQueue;
    private HttpClientBuilder builder;
    private int maxPerRoute;
    private int maxTotal;

    public POSTHandlerThreadPool(int poolStartSize) {
        busyThreads = new ArrayList<POSTHandlerThread>();
        availableThreadsQueue = new ConcurrentLinkedQueue<POSTHandlerThread>();
        
        builder = HttpClientBuilder.create();
        builder.setMaxConnPerRoute(poolStartSize);
        builder.setMaxConnTotal(poolStartSize * 2);
        maxPerRoute = poolStartSize;
        maxTotal = poolStartSize * 2;
        
        for (int i = 0; i < poolStartSize; i++) {
            POSTHandlerThread thread = new POSTHandlerThread(this, builder);
            thread.start();
            availableThreadsQueue.offer(thread);
        }
    }

    public POSTHandlerThreadPool() {
        this(5);
    }

    public void doRequest(POSTRequest request) {
        POSTHandlerThread thread = availableThreadsQueue.poll();
        if (thread == null) {
            if (maxTotal < maxPerRoute + 1) {
                maxTotal = (maxPerRoute + 1) * 2;
                builder.setMaxConnTotal(maxTotal);
            }
            builder.setMaxConnPerRoute(++maxPerRoute);
            thread = new POSTHandlerThread(this, builder);
            thread.start();
        }
        thread.startRequest(request);
    }

    protected void onThreadDone(POSTHandlerThread thread) {
        busyThreads.remove(thread);
        availableThreadsQueue.offer(thread);
    }

    public void stopThreads() {
        for (POSTHandlerThread cur : availableThreadsQueue) {
            cur.stopRunning();
        }
        while (!busyThreads.isEmpty()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Main.logDebugInfo(Level.SEVERE, "PostHandlerThreadPool interrupted while shutting down.", ex);
            }
        }
        for (POSTHandlerThread cur : availableThreadsQueue) {
            cur.stopRunning();
        }
    }
}
