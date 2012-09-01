package Waterflames.websend;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandParser
{
	// <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
	Settings settings;
	Server server;
	boolean debugMode;

	public CommandParser()
	{
		settings = Main.getSettings();
		server = Main.getBukkitServer();
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="PARSING - CATEGORY">
	public void parse(String line, Player player)
	{ // Now it's slightly better.
		if (line.contains(";"))
		{ // check split sign
			if (debugMode)
			{
				Main.getMainLogger().info("Websend: ';' found");
			}
			String[] lineArray = line.split(";"); // split line into seperate
													// command lines
			for (int i = 0; i < lineArray.length; i++)
			{ // for every command line
				if (lineArray[i].contains("/Command/"))
				{ // Check if line is an actual command line
					parseCommand(lineArray[i], player);
				}
				else if (lineArray[i].contains("/Output/"))
				{
					parseOutput(lineArray[i], player);
				}
				else
				{
					if (debugMode)
					{
						Main.getMainLogger().log(Level.WARNING, "Websend: No command or output tag found!");
					}
				}
			}
		}
		else
		{
			if (debugMode)
			{
				Main.getMainLogger().log(Level.WARNING, "Websend: No ; found.");
			}
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="PARSING - COMMAND">
	private void parseCommand(String line, Player player)
	{
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: A command line was found.");
		}
		String splittedLine[];
		splittedLine = line.split("/Command/"); // split command line into
												// '/command/' and actual
												// command
		if (splittedLine[1].contains("ExecutePlayerCommand:"))
		{
			onExecutePlayerCommand(player, splittedLine[1]);
		}
		else if (splittedLine[1].contains("ExecutePlayerCommand-"))
		{
			onExecutePlayerCommand(splittedLine[1]);
		}
		else if (splittedLine[1].contains("ExecuteConsoleCommand:"))
		{
			onExecuteConsoleCommand(player, splittedLine[1]);
		}
		else if (splittedLine[1].contains("ExecuteScript:"))
		{
			onExecuteScript(splittedLine[1]);
		}
		else if (splittedLine[1].contains("SetResponseURL:"))
		{
			onSetResponseURL(splittedLine[1]);
		}
		else
		{
			Main.getMainLogger().info("Websend ERROR: While parsing php output, websend found");
			Main.getMainLogger().info("an error on output line " + line + ": Invalid command.");
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="PARSING - OUTPUT">
	private void parseOutput(String line, Player player)
	{
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: An output line was found.");
		}
		String splittedLine[];
		splittedLine = line.split("/Output/"); // split command line into
												// '/Output/' and actual command
		if (splittedLine[1].contains("PrintToConsole:"))
		{
			onPrintToConsole(splittedLine[1]);
		}
		else if (splittedLine[1].contains("PrintToPlayer:"))
		{
			onPrintToPlayer(splittedLine[1], player);
		}
		else if (splittedLine[1].contains("PrintToPlayer-"))
		{
			onPrintToPlayer(splittedLine[1]);
		}
		else if (splittedLine[1].contains("Broadcast:"))
		{
			onBroadcast(splittedLine[1]);
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="EXECUTION - COMMAND">

	// <editor-fold defaultstate="collapsed" desc="onSetResponseURL">
	private void onSetResponseURL(String line)
	{
		String newURL = line.split("SetResponseURL:")[1];
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: Changed ResponseURL to " + newURL);
		}
		settings.setResponseURL(newURL);
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onExecutePlayerCommand">
	private void onExecutePlayerCommand(Player player, String line)
	{
		if (player == null)
		{
			Main.getMainLogger().info("Websend: ExecutePlayerCommand is used in a wrong context.");
		}
		String[] commandArray = line.split("ExecutePlayerCommand:");
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: An ExecutePlayerCommand was found: '" + Util.stringArrayToString(commandArray) + "'");
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Task(commandArray, player)
		{

			@Override
			public void run()
			{
				String[] commandArray = (String[]) this.getArgs().get(0);
				Player player = (Player) this.getArgs().get(1);
				try
				{
					if (player == null)
					{
						Main.getMainLogger().info("Command dispatching from terminal is not allowed. Try again in-game.");
					}
					else if (!player.getServer().dispatchCommand(player, commandArray[1]))
					{ // execute command and check for succes.
						player.sendMessage("Command dispatching failed: '" + commandArray[1] + "'"); // error
					}
				}
				catch (Exception ex)
				{
					Main.getMainLogger().info("An error has occured, are you trying to execute a player command from console?");
				}
			}

		});
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onExecutePlayerCommand">
	private void onExecutePlayerCommand(String line)
	{
		// split line into command and variables
		String[] commandArray = line.split("ExecutePlayerCommand-");
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: An ExecutePlayerCommand was found: '" + Util.stringArrayToString(commandArray) + "'");
		}

		Object[] taskArgs = new Object[] { commandArray };
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Task(taskArgs)
		{
			@Override
			public void run()
			{
				String argArray[] = ((String[]) this.getArgs().get(0))[1].split(":");
				Player fakePlayer = server.getPlayer(argArray[0].trim());
				if (!server.dispatchCommand(fakePlayer, argArray[1]))
				{ // execute command and check for succes.
					Main.getMainLogger().info("Command dispatching failed: '" + argArray[1] + "'"); // error
				}
			}
		});
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onExecuteScript">
	private void onExecuteScript(String line)
	{
		String scriptName = line.split(":")[1];
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Task(scriptName.trim())
		{
			@Override
			public void run()
			{
				Main.getScriptManager().invokeScript((String) this.getArgs().get(0));
			}
		});
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onExecuteConsoleCommand">
	private void onExecuteConsoleCommand(Player player, String line)
	{
		// split line into command and variables
		String[] commandArray = line.split("ExecuteConsoleCommand:");
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: An ExecuteConsoleCommand was found: '" + Util.stringArrayToString(commandArray) + "'");
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Task(commandArray, player)
		{
			@Override
			public void run()
			{
				String[] commandArray = (String[]) this.getArgs().get(0);
				Player player = (Player) this.getArgs().get(1);
				ConsoleCommandSender ccs = server.getConsoleSender();
				if (!server.dispatchCommand(ccs, commandArray[1]))
				{ // execute command and check for succes.
					if (player != null)
					{
						player.sendMessage("Command dispatching failed: '" + commandArray[1] + "'"); // error
					}
					else
					{
						Main.getMainLogger().info("Command dispatching failed: '" + commandArray[1] + "'"); // error
					}
				}
			}
		});
	}

	// </editor-fold>

        //<editor-fold defaultstate="collapsed" desc="onExecuteConsoleCommandAndReturn">
        private void onExecuteConsoleCommandAndReturn(String line)
        {
            // split line into command and variables
            String[] commandArray = line.split("ExecuteConsoleCommandAndReturn-");
            if (debugMode)
            {
                Main.getMainLogger().info("Websend: An ExecuteConsoleCommandAndReturn was found: '" + Util.stringArrayToString(commandArray) + "'");
            }
            Plugin plugin = null;
            if (commandArray[1].split(":")[0].toLowerCase().startsWith("bukkit"))
            {
                // TODO: implement bukkit listening.
            }
            else
            {
                plugin = server.getPluginManager().getPlugin(commandArray[1].split(":")[0]);
                if (plugin == null)
                {
                    Main.getMainLogger().info("ERROR: An invalid plugin name was provided.");
                    return;
                }
                //TODO: Implement or remove.
            }
        }
        //</editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onExecutePlayerCommandAndReturn">
	private void onExecutePlayerCommandAndReturn(String line)
	{
		String commandArray[];
		// split line into command and variables
		commandArray = line.split("ExecutePlayerCommandAndReturn-");
		if (debugMode)
		{
			Main.getMainLogger().info("Websend: An ExecutePlayerCommandAndReturn was found: '" + Util.stringArrayToString(commandArray) + "'");
		}
		String argArray[] = commandArray[1].split("-");
		Player fakePlayer = server.getPlayer(argArray[0].trim());
		String command = argArray[1].split(":")[1];
		Plugin plugin = null;
		if (commandArray[1].split(":")[0].toLowerCase().startsWith("bukkit"))
		{
			// TODO: implement bukkit listening.
		}
		else
		{
			plugin = server.getPluginManager().getPlugin(commandArray[1].split(":")[0]);
			if (plugin == null)
			{
				Main.getMainLogger().info("ERROR: An invalid plugin name was provided.");
				return;
			}
                        //TODO: Implement or remove.
		}
	}

	// </editor-fold>

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="EXECUTION - OUTPUT">
	// <editor-fold defaultstate="collapsed" desc="onPrintToConsole">
	private void onPrintToConsole(String line)
	{
		String text = line.replaceFirst("PrintToConsole:", "");
		Main.getMainLogger().info(text);
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onPrintToPlayer">
	private void onPrintToPlayer(String line, Player player)
	{
		if (player == null)
		{
			Main.getMainLogger().log(Level.WARNING, "Websend: No player to print text to. Use 'PrintToPlayer-playername: text' to sent text in this context.");
			Main.getMainLogger().log(Level.WARNING, line.replaceFirst("PrintToConsole:", ""));
		}
		else
		{
			String text = line.replaceFirst("PrintToPlayer:", "");
			player.sendMessage(parseColor(text));
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onPrintToPlayer">
	private void onPrintToPlayer(String line)
	{
		String commandData = line.replace("PrintToPlayer-", "");
		String[] commandDataArray = commandData.split(":");
		String playerName = commandDataArray[0];
		Player currentPlayer = server.getPlayer(playerName);
		if ("console".equals(playerName))
		{
			if (debugMode)
			{
				Main.getMainLogger().info("Websend: Player 'console'? Using PrintToConsole instead.");
			}
                        Main.getMainLogger().info(Util.stringArrayToString(commandDataArray));
		}
		else if (currentPlayer == null)
		{
			Main.getMainLogger().log(Level.WARNING, "Websend: No player '" + playerName + "' found on PrintToPlayer.");
		}
		else if (!currentPlayer.isOnline())
		{
			if (debugMode)
			{
				Main.getMainLogger().info("Websend: Player '" + playerName + "' is offline. Ignoring PrintToPlayer");
			}
		}
		else
		{
                        currentPlayer.sendMessage(parseColor(Util.stringArrayToString(commandDataArray)));
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="onBroadcast">
	private void onBroadcast(String line)
	{
		String text = line.replaceFirst("Broadcast:", "");
		server.broadcastMessage(parseColor(text));
	}

	// </editor-fold>
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="ETC - CHATCOLOR">
	public String parseColor(String line)
	{
		// Much easier, I promise.
		return ChatColor.translateAlternateColorCodes('&', line);
	}
	// </editor-fold>
}
