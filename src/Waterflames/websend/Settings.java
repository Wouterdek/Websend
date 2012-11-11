package Waterflames.websend;

public class Settings
{
	private String responseURL;
	private String password;
	private String salt = "";
	private String algoritm = "MD5";
	private int port;
	private boolean debugMode;
	private boolean serverActive;
	private String URL;

	public Settings()
	{
	}

	public Settings(String responseURL, String password, String salt, int port, boolean debugMode, boolean serverActive, String URL)
	{
		this.responseURL = responseURL;
		this.password = password;
		this.salt = salt;
		this.port = port;
		this.debugMode = debugMode;
		this.serverActive = serverActive;
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
        
        public String getHashingAlgoritm() {
                return this.algoritm;
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
        
        public void setHashingAlgoritm(String algoritm) {
                this.algoritm = algoritm;
        }
}
