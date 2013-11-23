package com.github.websend.post;

import com.github.websend.Main;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;

public class POSTHandlerThread extends Thread {

    private final POSTHandlerThreadPool parent;
    private final DefaultHttpClient httpClient;
    private boolean running = true;
    private boolean busy = false;
    private POSTRequest currentRequest;

    public POSTHandlerThread(POSTHandlerThreadPool parent, ClientConnectionManager connectionManager) {
        super("Websend POST Request Handler");
        this.httpClient = new DefaultHttpClient(connectionManager);
        this.parent = parent;
    }

    @Override
    public void run() {
        while (running) {
            if (currentRequest != null) {
                busy = true;
                try {
                    currentRequest.run(httpClient);
                } catch (Exception ex) {
                    Main.getMainLogger().log(Level.SEVERE, "An exception occured while running a POST request.", ex);
                }
                parent.onThreadDone(this);
                currentRequest = null;
                busy = false;
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(POSTHandlerThread.class.getName()).log(Level.SEVERE, "Thread interrupted.", ex);
                }
            }
        }
    }

    public void startRequest(POSTRequest request) {
        if (!busy) {
            currentRequest = request;
            busy = true;
        } else {
            throw new RuntimeException("Tried to assign request to busy thread!");
        }
    }

    public void stopRunning() {
        running = false;
    }

    public boolean isBusy() {
        return busy;
    }
}
