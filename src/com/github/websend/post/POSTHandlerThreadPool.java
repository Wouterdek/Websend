package com.github.websend.post;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

public class POSTHandlerThreadPool {
    private ArrayList<POSTHandlerThread> busyThreads;
    private ConcurrentLinkedQueue<POSTHandlerThread> availableThreadsQueue;
    private PoolingClientConnectionManager connectionManager;

    public POSTHandlerThreadPool(int poolStartSize) {
        busyThreads = new ArrayList<POSTHandlerThread>();
        availableThreadsQueue = new ConcurrentLinkedQueue<POSTHandlerThread>();
        
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setDefaultMaxPerRoute(poolStartSize);
        connectionManager.setMaxTotal(poolStartSize*2);
        for(int i = 0;i<poolStartSize;i++){
            POSTHandlerThread thread = new POSTHandlerThread(this, connectionManager);
            thread.start();
            availableThreadsQueue.offer(thread);
        }
    }
    
    public POSTHandlerThreadPool() {
        this(5);
    }
    
    public void doRequest(POSTRequest request){
        POSTHandlerThread thread = availableThreadsQueue.poll();
        if(thread == null){
            int curMaxTotal = connectionManager.getMaxTotal();
            int curMaxPerRoute = connectionManager.getDefaultMaxPerRoute();
            if(curMaxTotal < curMaxPerRoute + 1){
                connectionManager.setMaxTotal((curMaxPerRoute + 1)*2);
            }
            connectionManager.setDefaultMaxPerRoute(curMaxPerRoute + 1);
            thread = new POSTHandlerThread(this, connectionManager);
            thread.start();
        }
        thread.startRequest(request);
    }
    
    protected void onThreadDone(POSTHandlerThread thread){
        busyThreads.remove(thread);
        availableThreadsQueue.offer(thread);
    }
    
    public void stopThreads(){
        for(POSTHandlerThread cur : availableThreadsQueue){
            cur.stopRunning();
        }
        while(!busyThreads.isEmpty()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(POSTHandlerThreadPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for(POSTHandlerThread cur : availableThreadsQueue){
            cur.stopRunning();
        }
        connectionManager.shutdown();
    }
}
