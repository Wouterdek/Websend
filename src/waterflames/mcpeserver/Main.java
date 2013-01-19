package waterflames.mcpeserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import waterflames.mcpeserver.server.NetworkManager;

public class Main
{
	public static NetworkManager netManager;

	public static void main(String[] args)
	{
		Main.log("Minecraft Pocket Edition - Standalone Server v0.1");
		Main.log("Starting server. (UDP port 19132)");
		netManager = new NetworkManager();
		netManager.start();

		boolean running = true;
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);

		while (running)
		{
			String str = null;
			try
			{
				str = in.readLine();
			}
			catch (IOException ex)
			{
				Main.handleException(ex);
			}
			if (str.equals("stop"))
			{
				Main.log("Stopping server.");
				running = false;
				netManager.stopService();
				try
				{
					netManager.join(2000);
				}
				catch (InterruptedException ex)
				{
					Main.handleException(ex);
				}
			}
		}
	}

	public static void handleException(Exception ex)
	{
		Logger.getLogger("MinecraftPEServer").log(Level.WARNING, null, ex);
	}

	public static void log(String message)
	{
		Logger.getLogger("MinecraftPEServer").log(Level.INFO, message);
	}
}