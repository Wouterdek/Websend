package Waterflames.websend;

import com.Ostermiller.util.Base64;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class POSTHandler
{
	URL url;
	URL responseURL;
	String pass;
	String[] apacheAuthParts;
	Server server;
	Settings settings;
	boolean closed = false;

	public POSTHandler()
	{
		settings = Main.settings;
		server = Main.bukkitServer;
		pass = settings.getPassword();

		// Main url
		String urlString = settings.getURL();
		if (urlString == null || "".equals(urlString.trim()))
		{
			Main.logger.log(Level.SEVERE, "No url was found. Please check your configuration file.");
			return;
		}
		try
		{
			if (urlString.contains("@"))
			{
				String[] urlParts = urlString.split("@");
				urlParts[0] = urlParts[0].replace("http://", "");
				apacheAuthParts = urlParts[0].split(":");
			}
			url = new URL(urlString);
		}
		catch (MalformedURLException ex)
		{
			Main.logger.log(Level.SEVERE, "Websend: Error while parsing URL: " + urlString, ex);
		}

		// Reponse url
		if (settings.getResponseURL() != null)
		{
			String responseURLString = (String) settings.getResponseURL();
			try
			{
				responseURL = new URL(responseURLString);
			}
			catch (MalformedURLException ex)
			{
				Main.logger.log(Level.SEVERE, "Websend: Error while parsing response URL: " + urlString, ex);
			}
		}
	}

	public void setURL(String urlArg)
	{
		try
		{
			url = new URL(urlArg);
		}
		catch (MalformedURLException ex)
		{
			Main.logger.log(Level.SEVERE, "Websend: Error while parsing URL: " + urlArg, ex);
		}
	}

	public void sendPOST(String args[], Player player, String playerNameArg, boolean isResponse) throws Exception
	{
		String argsEncoded[] = new String[args.length];
		for (int i = 0; i < args.length; i++)
		{
			argsEncoded[i] = URLEncoder.encode(args[i], "UTF-8");
		}

		URLConnection con = url.openConnection();
		con.setRequestProperty("Host", Main.bukkitServer.getIp());
		con.setRequestProperty("User-Agent", Main.plugin.getDescription().getFullName());
		if (apacheAuthParts != null)
		{
			con.setRequestProperty("Authorization", "Basic " + Base64.encode(apacheAuthParts[0] + ":" + apacheAuthParts[1]));
		}
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		if (isResponse == true)
		{
			out.write("isResponse=" + URLEncoder.encode("true", "UTF-8") + "&");
		}
		else
		{
			out.write("isResponse=" + URLEncoder.encode("false", "UTF-8") + "&");
		}
		sendData(out, player, playerNameArg);
		for (int i = 0; i < argsEncoded.length; i++)
		{
			out.write("args[" + i + "]=" + argsEncoded[i] + "&");
		}
		out.write("authKey=" + URLEncoder.encode(hash(pass), "UTF-8"));
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		CommandParser parser = new CommandParser();
		String line;
		while ((line = in.readLine()) != null)
		{
			parser.parse(line, player);
		}
		in.close();
	}

	public void sendData(OutputStreamWriter out, Player player, String playerNameArg) throws UnsupportedEncodingException, IOException
	{
		// Player data
		if (player != null)
		{
			String playerName = URLEncoder.encode(player.getName(), "UTF-8");
			out.write("player=" + playerName + "&");
			out.write("playerWorld=" + URLEncoder.encode(player.getWorld().getName(), "UTF-8") + "&");
			out.write("playerX=" + URLEncoder.encode(player.getLocation().getX() + "", "UTF-8") + "&");
			out.write("playerY=" + URLEncoder.encode(player.getLocation().getY() + "", "UTF-8") + "&");
			out.write("playerZ=" + URLEncoder.encode(player.getLocation().getZ() + "", "UTF-8") + "&");
			out.write("playerYaw=" + URLEncoder.encode(player.getLocation().getYaw() + "", "UTF-8") + "&");
			out.write("playerPitch=" + URLEncoder.encode(player.getLocation().getPitch() + "", "UTF-8") + "&");
			out.write("playerXP=" + URLEncoder.encode(player.getExp() + "", "UTF-8") + "&");
			out.write("playerXPLevel=" + URLEncoder.encode(player.getLevel() + "", "UTF-8") + "&");
			out.write("playerExhaustion=" + URLEncoder.encode(player.getExhaustion() + "", "UTF-8") + "&");
			out.write("playerFoodLevel=" + URLEncoder.encode(player.getFoodLevel() + "", "UTF-8") + "&");
			out.write("playerGameMode=" + URLEncoder.encode(player.getGameMode() + "", "UTF-8") + "&");
			out.write("playerHealth=" + URLEncoder.encode(player.getHealth() + "", "UTF-8") + "&");

			String unModdedIP = player.getAddress().getAddress().getHostAddress();
			out.write("playerIP=" + URLEncoder.encode(unModdedIP.replace("/", ""), "UTF-8") + "&");

			out.write("playerIsOP=" + URLEncoder.encode(Boolean.toString(player.isOp()), "UTF-8") + "&");
			out.write("playerCurrentItemIndex=" + URLEncoder.encode(String.valueOf(player.getInventory().getHeldItemSlot()), "UTF-8") + "&");
			out.write("playerCurrentItemID=" + URLEncoder.encode(String.valueOf(player.getItemInHand().getTypeId()), "UTF-8") + "&");
			for (int i = 0; i < player.getInventory().getSize(); i++)
			{
				try
				{
					ItemStack item = player.getInventory().getItem(i);
					out.write("playerInventoryTypes[" + i + "]=" + URLEncoder.encode(item.getTypeId() + "", "UTF-8") + "&");
					out.write("playerInventoryAmounts[" + i + "]=" + URLEncoder.encode(item.getAmount() + "", "UTF-8") + "&");

					Iterator<Enchantment> enchIter = item.getEnchantments().keySet().iterator();

					int enchIndex = 0;
					while (enchIter.hasNext())
					{
						Enchantment cur = enchIter.next();
						out.write("playerInventoryEnchantments[" + i + "][" + enchIndex + "]=" + cur.getName() + "&");
						out.write("playerInventoryEnchantmentsLevels[" + i + "][" + enchIndex + "]=" + item.getEnchantmentLevel(cur) + "&");
						enchIndex++;
					}
					if (item.getData().getData() != 0)
					{
						out.write("playerInventoryData[" + i + "]=" + URLEncoder.encode(item.getData().getData() + "", "UTF-8") + "&");
					}
				}
				catch (NullPointerException ex)
				{
				}
			}
		}
		else if (playerNameArg != null)
		{
			String playerName = URLEncoder.encode(playerNameArg, "UTF-8");
			out.write("player=" + playerName + "&");
		}
		else
		{
			String playerName = URLEncoder.encode("console", "UTF-8");
			out.write("player=" + playerName + "&");
		}
		// Player list
		for (int i = 0; i < server.getOnlinePlayers().length; i++)
		{
			out.write("onlinePlayers[" + i + "]=" + URLEncoder.encode(server.getOnlinePlayers()[i].getName(), "UTF-8") + "&");
			out.write("onlinePlayersIP[" + i + "]=" + URLEncoder.encode(server.getOnlinePlayers()[i].getAddress().getHostName(), "UTF-8") + "&");
		}
		out.write("maxPlayers=" + URLEncoder.encode(String.valueOf(server.getMaxPlayers()), "UTF-8") + "&");
		out.write("curPlayers=" + URLEncoder.encode(String.valueOf(server.getOnlinePlayers().length), "UTF-8") + "&");
		// RAM
		out.write("availableMemory=" + URLEncoder.encode(String.valueOf(Runtime.getRuntime().freeMemory()), "UTF-8") + "&");
		out.write("maxMemory=" + URLEncoder.encode(String.valueOf(Runtime.getRuntime().maxMemory()), "UTF-8") + "&");
		// PluginList
		for (int i = 0; i < server.getPluginManager().getPlugins().length; i++)
		{
			out.write("pluginList[" + i + "]=" + URLEncoder.encode(server.getPluginManager().getPlugins()[i].getDescription().getFullName(), "UTF-8") + "&");
		}
		// Misc
		out.write("bukkitBuild=" + URLEncoder.encode(server.getVersion(), "UTF-8") + "&");
		out.write("bukkitPort=" + URLEncoder.encode(String.valueOf(server.getPort()), "UTF-8") + "&");
		out.write("serverName=" + URLEncoder.encode(String.valueOf(server.getServerName()), "UTF-8") + "&");
		out.write("netherEnabled=" + URLEncoder.encode(String.valueOf(server.getAllowNether()), "UTF-8") + "&");
		out.write("flyingEnabled=" + URLEncoder.encode(String.valueOf(server.getAllowFlight()), "UTF-8") + "&");
		out.write("defaultGameMode=" + URLEncoder.encode(String.valueOf(server.getDefaultGameMode()), "UTF-8") + "&");
		out.write("onlineMode=" + URLEncoder.encode(String.valueOf(server.getOnlineMode()), "UTF-8") + "&");
	}

	public void close()
	{
		closed = true;
	}

	private String hash(String input)
	{
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException ex)
		{
			Main.logger.info("Websend: Failed to hash password to MD5");
		}
		md.update(input.getBytes());
		BigInteger bigInt = new BigInteger(1, md.digest());
		String result = bigInt.toString(16);
		if ((result.length() % 2) != 0)
		{
			result = "0" + result;
		}
		return result;
	}
}
