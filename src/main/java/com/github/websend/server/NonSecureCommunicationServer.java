package com.github.websend.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class NonSecureCommunicationServer extends CommunicationServer{
    @Override
    ServerSocket openServerSocket(InetAddress bindIP, int port) throws IOException {
        return new ServerSocket(port, 0, bindIP);
    }

    @Override
    ServerSocket openServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
}
