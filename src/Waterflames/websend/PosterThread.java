package Waterflames.websend;

import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class PosterThread extends Thread
{
	Settings settings;
	Server server;
	String[] args;
	Player player;
	String playerName;
	String url;
	boolean isResponse;

	public PosterThread()
	{
		settings = Main.settings;
		server = Main.bukkitServer;
	}

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
		if (url != null)
		{
			postHandler.setURL(url);
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
		catch (Exception ex)
		{
			Main.logger.log(Level.SEVERE, "An error occured while trying to do a bukkit -> php connection. (POST)", ex);
		}
	}
}
