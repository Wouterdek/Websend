package Waterflames.websend;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigHandler
{
	Logger logger;

	public ConfigHandler()
	{
		this.logger = Main.logger;
	}

	public Settings loadSettings() throws FileNotFoundException, IOException
	{
		// Prepare new settings map
		Settings settings = new Settings();

		// Set default values
		settings.setPort(4445);
		settings.setDebugMode(false);
		settings.setServerActive(false);

		// Open file
		BufferedReader reader = openFile();

		// Parse each line if line is not null
		String currentLine;
		while ((currentLine = reader.readLine()) != null)
		{
			parseLine(currentLine, settings);
		}

		return settings;
	}

	public void generateConfig()
	{
		// File declaration
		File websendDir = Main.plugin.getDataFolder();
		File configFile = new File(websendDir, "config.txt");

		// Prepare file
		PrintWriter writer = null;
		try
		{
			configFile.createNewFile();
			writer = new PrintWriter(new FileWriter(configFile));
		}
		catch (IOException ex)
		{
			logger.info("Websend failed to create a new configuration file.");
			logger.log(Level.SEVERE, null, ex);
		}

		// Fill file
		writer.println("#Configuration and settings file!");
		writer.println("#Help: PASS: change the password to one of your choice (set the same in the server php file).");
		writer.println("#Help: DEBUG_WEBSEND: shows debugging messages for easier tracking of bugs.");
		writer.println("#Help: SALT: adds a salt to the hashed password when sending over bukkit -> php connection.");
		writer.println("PASS=YourPassHere");
		writer.println("#Optional settings. Remove the '#' to use.");
		writer.println("#ALTPORT=1234");
		writer.println("#DEBUG_WEBSEND=false/true");
		writer.println("#SALT=abc123");
		writer.close();
	}

	private BufferedReader openFile() throws FileNotFoundException
	{
		// File declaration
		File folder = Main.plugin.getDataFolder();
		File configFile = new File(folder, "config.txt");

		// Reader opening
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		return reader;
	}

	private void parseLine(String line, Settings settings)
	{
		// Is the line a comment?
		if (line.trim().startsWith("#"))
		{
			return;
		}
		else
		{
			// What value does this line contain?
			if (line.startsWith("RESPONSEURL="))
			{
				// Clean and store value
				String value = line.replaceFirst("RESPONSEURL=", "");
				settings.setResponseURL(value);
			}
			else if (line.startsWith("PASS="))
			{
				String value = line.replaceFirst("PASS=", "");
				settings.setPassword(value);
			}
			else if (line.startsWith("ALTPORT="))
			{
				String value = line.replaceFirst("ALTPORT=", "");
				int convertedValue = 0;
				try
				{
					convertedValue = Integer.parseInt(value.trim());
					if (convertedValue == Main.port)
					{
						logger.log(Level.WARNING, "Websend error: You are trying to host websend on the server port!");
					}
				}
				catch (Exception ex)
				{
					logger.log(Level.SEVERE, "Websend failed to parse your new port value:" + value, ex);
					return;
				}
				settings.setPort(convertedValue);
			}
			else if (line.startsWith("DEBUG_WEBSEND="))
			{
				String value = line.replaceFirst("DEBUG_WEBSEND=", "");
				if (value.toLowerCase().trim().contains("true"))
				{
					settings.setDebugMode(true);
				}
				else
				{
					settings.setDebugMode(false);
				}
			}
			else if (line.startsWith("WEBLISTENER_ACTIVE="))
			{
				String value = line.replaceFirst("WEBLISTENER_ACTIVE=", "");
				if (value.toLowerCase().trim().contains("true"))
				{
					settings.setServerActive(true);
				}
				else
				{
					settings.setServerActive(false);
				}
			}
			else if (line.startsWith("URL="))
			{
				String value = line.replaceFirst("URL=", "");
				settings.setURL(value);
			}
			else if (line.startsWith("SALT="))
			{
				String value = line.replaceFirst("SALT=", "");
				settings.setSalt(value);
			}
			else
			{
				logger.info("WEBSEND ERROR: Error while parsing config file.");
				logger.info("Invalid line: " + line);
			}
		}
	}
}
