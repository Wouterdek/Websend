package com.github.websend.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SecureCommunicationServer extends CommunicationServer{
    @Override
    ServerSocket openServerSocket(InetAddress bindIP, int port) throws IOException {
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port, 0, bindIP);
        return sslserversocket;
    }

    @Override
    ServerSocket openServerSocket(int port) throws IOException {
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
        return sslserversocket;
    }
}
