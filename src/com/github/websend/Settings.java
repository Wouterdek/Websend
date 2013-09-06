package com.github.websend;

public class Settings
{
	private String responseURL;
	private String password;
	private String salt = "";
	private String algorithm = "MD5";
	private int port;
	private boolean debugMode = false;
	private boolean gzipRequests = false;
	private boolean serverActive = false;
	private String URL;

	public Settings()
	{
	}

	public Settings(String responseURL, String password, String salt, int port, boolean debugMode, boolean serverActive, boolean gzipRequests, String URL)
	{
		this.responseURL = responseURL;
		this.password = password;
		this.salt = salt;
		this.port = port;
		this.debugMode = debugMode;
		this.serverActive = serverActive;
            this.gzipRequests = gzipRequests;
		this.URL = URL;
	}

	public String getURL()
	{
		return URL;
	}

	public boolean isDebugMode()
	{
		return debugMode;
	}

	public String getPassword()
	{
		return password;
	}

	public int getPort()
	{
		return port;
	}

	public String getResponseURL()
	{
		return responseURL;
	}

	public String getSalt()
	{
		return salt;
	}

	public String getHashingAlgorithm()
	{
		return this.algorithm;
	}

	public boolean isServerActive()
	{
		return serverActive;
	}

	public void setURL(String URL)
	{
		this.URL = URL;
	}

	public void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setResponseURL(String responseURL)
	{
		this.responseURL = responseURL;
	}

	public void setSalt(String salt)
	{
		this.salt = salt;
	}

	public void setServerActive(boolean serverActive)
	{
		this.serverActive = serverActive;
	}

	public void setHashingAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}

      public boolean areRequestsGZipped() {
            return gzipRequests;
      }

      public void setGzipRequests(boolean gzipRequests) {
            this.gzipRequests = gzipRequests;
      }
}
