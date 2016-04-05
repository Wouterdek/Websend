package com.github.websend;

import java.net.InetAddress;

public class Settings {

    private String responseURL;
    private String password;
    private String salt = "";
    private String algorithm = "SHA-512";
    private int port = 4445;
    private boolean debugMode = false;
    private boolean gzipRequests = false;
    private boolean serverActive = false;
    private String URL;
    private InetAddress serverBindIP = null;
    private boolean wrapCommandExecutor;
    private boolean sslEnabled = false;
    private String sslPassword = null;
    private boolean extendedPlayerDataEnabled;
    
    public String getURL() {
        return URL;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getResponseURL() {
        return responseURL;
    }

    public String getSalt() {
        return salt;
    }

    public String getHashingAlgorithm() {
        return this.algorithm;
    }

    public boolean isServerActive() {
        return serverActive;
    }

    public InetAddress getServerBindIP() {
        return serverBindIP;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setResponseURL(String responseURL) {
        this.responseURL = responseURL;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setServerActive(boolean serverActive) {
        this.serverActive = serverActive;
    }

    public void setHashingAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean areRequestsGZipped() {
        return gzipRequests;
    }

    public void setGzipRequests(boolean gzipRequests) {
        this.gzipRequests = gzipRequests;
    }

    public void setServerBindIP(InetAddress ip) {
        this.serverBindIP = ip;
    }

    public boolean areCommandExecutorsWrapped() {
        return wrapCommandExecutor;
    }

    public void setWrapCommandExecutor(boolean b) {
        this.wrapCommandExecutor = b;
    }

    public boolean isSSLEnabled() {
        return sslEnabled;
    }

    public void setSSLEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public boolean isExtendedPlayerDataEnabled() {
        return extendedPlayerDataEnabled;
    }
    
    public void setExtendedPlayerDataEnabled(boolean enabled){
        this.extendedPlayerDataEnabled = enabled;
    }
    public String getSSLPassword() {
        return this.sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }
}
