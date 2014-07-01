/*
 * Packets: 1: DoCommandAsPlayer 2: DoCommandAsConsole 3: DoScript 4:
 * StartPluginOutputRecording 5: EndPluginOutputRecording 10:
 * WriteOutputToConsole 11: WriteOutputToPlayer 12: Broadcast 20: Disconnect 21:
 * Password
 */
package com.github.websend.server;

import com.github.websend.Main;
import com.github.websend.PacketHandler;
import com.github.websend.TrustedHosts;
import com.github.websend.server.remotejava.Session;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommunicationServer extends Thread {
    private static final int MAX_FAILS = 15;
    private static final int FAILURE_SLEEP_TIME = 15000;
    private final HashMap<Byte, PacketHandler> customPacketHandlers = new HashMap<Byte, PacketHandler>();
    private boolean running = true;
    private boolean connected = false;
    private boolean authenticated = false;
    private ServerSocket serverSkt;
    private PacketParser parser = new PacketParser(this);
    private ArrayList<Session> remoteJavaSessions = new ArrayList<Session>();
    
    abstract ServerSocket openServerSocket(InetAddress bindIP, int port) throws IOException;
    abstract ServerSocket openServerSocket(int port) throws IOException;
    
    @Override
    public void run() {
        int fails = 0;
        while (running) {
            if (fails == MAX_FAILS) {
                try {
                    Main.getMainLogger().info("Max amount of fails reached. Waiting for " + (FAILURE_SLEEP_TIME / 1000) + " seconds until retry.");
                    Thread.sleep(FAILURE_SLEEP_TIME);
                    fails = 0;
                } catch (InterruptedException ex) {
                    Logger.getLogger(CommunicationServer.class.getName()).log(Level.SEVERE, "Failed to sleep", ex);
                }
            }
            try {
                Main.logDebugInfo(Level.INFO, "Starting server");
                startServer();
            } catch (Exception ex) {
                Main.getMainLogger().log(Level.SEVERE, "Server encountered an error. Attempting restart.", ex);
                connected = false;
                authenticated = false;

                try {
                    serverSkt.close();
                } catch (IOException ex1) {
                    Main.logDebugInfo(Level.WARNING, "Failed to close server.", ex1);
                }
            }
        }
    }

    public void addPacketHandler(PacketHandler wph) {
        customPacketHandlers.put(wph.getHeader(), wph);
    }

    private void startServer() throws IOException {
        running = true;
        if (Main.getSettings().getServerBindIP() != null) {
            serverSkt = openServerSocket(Main.getSettings().getServerBindIP(), Main.getSettings().getPort());
        } else {
            serverSkt = openServerSocket(Main.getSettings().getPort());
        }

        while (running) {
            Main.logDebugInfo("Waiting for client.");
            Socket skt = serverSkt.accept();
            Main.logDebugInfo("Client connected.");
            if (TrustedHosts.isTrusted(skt.getInetAddress())) {
                Main.logDebugInfo("Client is trusted.");
                skt.setKeepAlive(true);
                ComplexInputStream in = new ComplexInputStream(skt.getInputStream());
                ComplexOutputStream out = new ComplexOutputStream(skt.getOutputStream());
                connected = true;

                Main.logDebugInfo("Trying to read first byte...");
                try {
                    if (in.readByte() == 21) {
                        Main.logDebugInfo("First packet is authentication request packet.");
                        
                        authenticated = parser.parseAuthenticationRequestPacket(in, out);
                        
                        if (!authenticated) {
                            Main.getMainLogger().log(Level.INFO, "Client failed to authenticate! Disconnecting.");
                            connected = false;
                        } else {
                            Main.logDebugInfo("Password is correct! Client connected.");
                        }
                    } else {
                        Main.getMainLogger().log(Level.WARNING, "First packet wasn't a authentication request packet! Disconnecting. (Are you using the correct version?)");
                        connected = false;
                    }

                    while (connected) {
                        byte packetHeader = in.readByte();
                        if (packetHeader == 1) {
                            Main.logDebugInfo("Got packet header: DoCommandAsPlayer");
                            parser.parseDoCommandAsPlayer(in, out);
                        } else if (packetHeader == 2) {
                            Main.logDebugInfo("Got packet header: DoCommandAsConsole");
                            parser.parseDoCommandAsConsole(in, out);
                        } else if (packetHeader == 3) {
                            Main.logDebugInfo("Got packet header: DoScript");
                            parser.parseDoScript(in, out);
                        } else if (packetHeader == 4) {
                            Main.logDebugInfo("Got packet header: StartPluginOutputRecording");
                            parser.parseStartPluginOutputRecording(in, out);
                        } else if (packetHeader == 5) {
                            Main.logDebugInfo("Got packet header: EndPluginOutputRecording");
                            parser.parseEndPluginOutputRecording(in, out);
                        } else if (packetHeader == 10) {
                            Main.logDebugInfo("Got packet header: WriteOutputToConsole");
                            parser.parseWriteOutputToConsole(in, out);
                        } else if (packetHeader == 11) {
                            Main.logDebugInfo("Got packet header: WriteOutputToPlayer");
                            parser.parseWriteOutputToPlayer(in, out);
                        } else if (packetHeader == 12) {
                            Main.logDebugInfo("Got packet header: Broadcast");
                            parser.parseBroadcast(in, out);
                        }else if (packetHeader >= 30 && packetHeader < 38) {
                            Main.logDebugInfo("Got packet header: Remote Java Request");
                            parser.parseRemoteJavaRequest(packetHeader, in, out);
                        } else if (packetHeader == 20) {
                            Main.logDebugInfo("Got packet header: Disconnect");
                            connected = false;
                        } else if (customPacketHandlers.containsKey(packetHeader)) {
                            Main.logDebugInfo("Got custom packet header: " + packetHeader);
                            customPacketHandlers.get(packetHeader).onHeaderReceived(in, out);
                        } else {
                            Main.getMainLogger().log(Level.WARNING, "Unsupported packet header!");
                        }
                    }
                    Main.logDebugInfo("Closing connection with client.");
                    out.flush();
                    out.close();
                    in.close();
                } catch (IOException ex) {
                    Main.getMainLogger().log(Level.WARNING, "IOException while communicating to client! Disconnecting. ("+ex.getMessage()+")");
                    connected = false;
                }
            } else {
                Main.getMainLogger().log(Level.WARNING, "Connection request from unauthorized address!");
                Main.getMainLogger().log(Level.WARNING, "Address: " + skt.getInetAddress());
                Main.getMainLogger().log(Level.WARNING, "Add this address to trusted.txt to allow access.");
            }
            skt.close();
        }
        serverSkt.close();
    }
    
    //Remote java
    public int openRemoteJavaSession(){
        if(remoteJavaSessions.add(new Session())){
            return remoteJavaSessions.size() - 1;
        }else{
            return -1;
        }
    }
    
    public Session getRemoteJavaSession(int sessionID){
        return remoteJavaSessions.get(sessionID);
    }
    
    public void closeRemoteJavaSession(int sessionID){
        Session session = remoteJavaSessions.get(sessionID);
        if(session != null){
            session.close();
            remoteJavaSessions.remove(sessionID);
        }
    }

    public void stopServer() {
        running = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
