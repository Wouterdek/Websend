package waterflames.websend;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginLoggerListener;

public class PluginOutputManager
{
	private static HashMap<String, ArrayList<String>> pluginOutputHashMap = new HashMap<String, ArrayList<String>>();

	public static void startRecording(String pluginName)
	{
		pluginOutputHashMap.put(pluginName, new ArrayList<String>());
	}

	public static ArrayList<String> stopRecording(String pluginName)
	{
		ArrayList<String> result = pluginOutputHashMap.get(pluginName);
		pluginOutputHashMap.remove(pluginName);
		return result;
	}

	public static void handleLogRecord(Plugin plugin, LogRecord logRecord)
	{
		if (pluginOutputHashMap.containsKey(plugin.getName()))
		{
			pluginOutputHashMap.get(plugin.getName()).add(logRecord.getMessage());
		}
	}

	public static void registerLoggerListener()
	{
		try
		{
			PluginLogger.registerGlobalListener(new PluginLoggerListener()
			{
				@Override
				public void onLogged(Plugin plugin, LogRecord lr)
				{
					PluginOutputManager.handleLogRecord(plugin, lr);
				}
			});
		}
		catch (Exception ex)
		{
			Main.getMainLogger().log(Level.INFO, "Default craftbukkit detected, plugin output capturing will not work.");
		}
	}

	// unused useful functions

	public static void hijackSystemOutputStream()
	{
		PrintStream newOutput = new PrintStream(System.out)
		{
			@Override
			public void println(String x)
			{
				Throwable t = new Throwable();
				super.println("Class " + t.getStackTrace()[1].getClassName() + " says: " + x);
			}
		};
		System.setOut(newOutput);
	}

	public static void hijackLogger(String loggerName)
	{
		Handler handler = new Handler()
		{
			@Override
			public void publish(LogRecord record)
			{
				record.setMessage("Class " + record.getSourceClassName() + " says: " + record.getMessage());
			}

			@Override
			public void flush()
			{
			}

			@Override
			public void close() throws SecurityException
			{
			}
		};

		// May give problems with openJDK
		Logger.getLogger(loggerName).addHandler(handler);
	}
}
