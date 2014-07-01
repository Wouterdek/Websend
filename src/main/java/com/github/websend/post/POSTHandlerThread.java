package com.github.websend.post;

import com.github.websend.Main;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class POSTHandlerThread extends Thread {
    private final POSTHandlerThreadPool parent;
    private final CloseableHttpClient httpClient;
    private boolean running = true;
    private boolean busy = false;
    private POSTRequest currentRequest;
    
    public POSTHandlerThread(POSTHandlerThreadPool parent, HttpClientBuilder builder) {
        super("Websend POST Request Handler");
        this.httpClient = builder.build();
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
                    Main.logError("An exception occured while running a POST request.", ex);
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
        try {
            httpClient.close();
        } catch (IOException ex) {
            Main.logDebug(Level.WARNING, "An exception occured while closing the httpclient.", ex);
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
