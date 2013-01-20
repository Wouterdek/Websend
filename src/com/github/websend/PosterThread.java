package com.github.websend;

import java.net.SocketTimeoutException;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class PosterThread extends Thread
{
	String[] args;
	Player player;
	String playerName;
	String url;
	boolean isResponse;

	public void setVariables(String[] newArgs, Player newPlayer, boolean isResponseArg)
	{
		args = newArgs;
		player = newPlayer;
		isResponse = isResponseArg;
	}

	public void setVariables(String[] newArgs, String newPlayer, boolean isResponseArg)
	{
		args = newArgs;
		playerName = newPlayer;
		isResponse = isResponseArg;
	}

	public void setURL(String urlArg)
	{
		url = urlArg;
	}

	@Override
	public void run()
	{
		POSTHandler postHandler = new POSTHandler();

		// If this.url is setup, replace url in posthandler.
		if (url != null)
		{
			postHandler.setURL(url);
		}

		if (!postHandler.setupVariables())
		{
			return;
		}

		try
		{
			if (player == null && playerName != null)
			{
				postHandler.sendPOST(args, player, playerName, isResponse);
			}
			else
			{
				postHandler.sendPOST(args, player, null, isResponse);
			}
		}
		catch (SocketTimeoutException ex)
		{
			Main.getMainLogger().log(Level.SEVERE, "The page took too long to respond! (loading time > 10 seconds)", ex);
		}
		catch (Exception ex)
		{
			Main.getMainLogger().log(Level.SEVERE, "An error occured while trying to do a bukkit -> php connection. (POST)", ex);
		}
	}
}
