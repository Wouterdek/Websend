package Waterflames.websend;

import Waterflames.websend.script.ScriptManager;
import Waterflames.websend.server.CommunicationServer;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
	public static Settings settings;
	public static Logger logger;
	public static Server bukkitServer;
	public static Main plugin;
	public static File scriptsDir;
	public static int port;
	public static ScriptManager scriptManager;
	public static CommunicationServer server;

	@Override
	public File getDataFolder()
	{
		String root = new File("").getAbsolutePath();
		File pluginDir = new File(root, "plugins");
		File websendDir = new File(pluginDir, "Websend");
		if (!websendDir.exists())
		{
			websendDir.mkdir();
		}
		return websendDir;
	}

	@Override
	public void onEnable()
	{
		// Setup vars
		logger = this.getLogger();
		bukkitServer = this.getServer();
		plugin = this;
		port = this.getServer().getPort();
		boolean needsSetup = false;

		// Parse config
		ConfigHandler configHandler = new ConfigHandler();
		try
		{
			settings = configHandler.loadSettings();
		}
		catch (FileNotFoundException ex)
		{
			configHandler.generateConfig();
			logger.info("Websend generated a config file. Go edit it!");
			needsSetup = true;
		}
		catch (IOException ex)
		{
			logger.info("Websend failed to read your configuration file.");
			logger.log(Level.SEVERE, null, ex);
			return;
		}

		try
		{
			File trustedFile = new File(this.getDataFolder(), "trusted.txt");
			if (!trustedFile.exists())
			{
				TrustedHosts.writeDefaultFile(trustedFile);
			}
			TrustedHosts.load(trustedFile);
		}
		catch (IOException ex)
		{
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}

		// Setup scripts
		scriptsDir = new File(this.getDataFolder(), "scripts");
		scriptManager = new ScriptManager();
		if (scriptsDir.exists())
		{
			scriptManager.loadScripts();
		}
		else
		{
			scriptsDir.mkdir();
			new File(scriptsDir, "compiled").mkdir();
		}

		if (needsSetup)
		{
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Start server
		if (settings.isServerActive())
		{
			server = new CommunicationServer();
			server.start();
		}
		try
		{
			PluginOutputManager.registerLoggerListener();
		}
		catch (NoClassDefFoundError ex)
		{
			logger.log(Level.WARNING, "Default bukkit detected, plugin output capturing may not function properly.");
		}
		// PluginOutputManager.hijackSystemOutputStream();
		// PluginOutputManager.hijackLogger();
	}

	@Override
	public void onDisable()
	{
		if (server != null)
		{
			server.stopServer();
		}
	}

	public static void doCommand(String[] args, Player player)
	{
		PosterThread poster = new PosterThread();
		poster.setVariables(args, player, false);
		poster.start();
	}

	public static void doCommand(String[] args, String str)
	{
		PosterThread poster = new PosterThread();
		poster.setVariables(args, str, false);
		poster.start();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("websend") || cmd.getName().equalsIgnoreCase("ws"))
		{
			if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
			{
				PosterThread poster = new PosterThread();
				poster.setVariables(args, "console", false);
				poster.start();
				return true;
			}
			else if (sender instanceof Player)
			{
				Player plsender = (Player) sender;
				// try{
				if (plsender.hasPermission("websend"))
				{
					PosterThread poster = new PosterThread();
					if (args.length > 0)
					{
						if (args[0].contains("-wp:"))
						{
							if (plsender.isOp())
							{
								poster.setURL(args[0].split(":")[1].trim());
							}
						}
					}
					poster.setVariables(args, plsender, false);
					poster.start();
					return true;
				}
				else
				{
					plsender.sendMessage("You are not allowed to use this command.");
				}
			}
		}
		else if (cmd.getName().equalsIgnoreCase("wsScripts"))
		{
			if (sender.hasPermission("websend.scripts"))
			{
				if (args.length < 1)
				{
					sender.sendMessage(" /*/ Websend Scripts Menu /*/");
					sender.sendMessage("    -wsScripts reload");
					sender.sendMessage("       Reloads the scripts in the scripts folder.");
					sender.sendMessage("    -wsScripts reload <scriptname>");
					sender.sendMessage("       Reloads the script.");
					sender.sendMessage("    -wsScripts list");
					sender.sendMessage("       Lists the currently loaded scripts.");
				}
				else if (args[0].equals("reload"))
				{
					if (args.length < 2)
					{
						scriptManager.reload();
					}
					else
					{
						scriptManager.reload(args[1]);
					}
					sender.sendMessage("Reload complete.");
				}
				else if (args[0].equals("list"))
				{
					sender.sendMessage("Currently loaded scripts:");
					for (String string : scriptManager.getScriptNames())
					{
						sender.sendMessage("   -" + string);
					}
				}
				else if (scriptManager.hasScript(args[0]))
				{
					scriptManager.invokeScript(args[0]);
				}
				else
				{
					sender.sendMessage("/wsscript " + args[0] + " does not exist.");
				}
			}
			else
			{
				sender.sendMessage("You are not allowed to use this command.('websend.scripts')");
			}
			return true;
		}
		return false;
	}
}
