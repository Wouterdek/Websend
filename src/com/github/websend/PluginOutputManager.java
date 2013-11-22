package com.github.websend;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginOutputManager {

    private static HashMap<String, ArrayList<String>> pluginOutputHashMap = new HashMap<String, ArrayList<String>>();
    private static boolean handlersRegistered = false;

    public static void registerPluginLoggerHandlers() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            plugin.getLogger().addHandler(new WebsendPluginLoggerHandler(plugin));
        }
    }

    public static void startRecording(String pluginName) {
        Main.logDebugInfo("Starting output recording of plugin " + pluginName);

        //register handlers on first use to prevent missing any plugin on load.
        if (!handlersRegistered) {
            PluginOutputManager.registerPluginLoggerHandlers();
            handlersRegistered = true;
        }
        pluginOutputHashMap.put(pluginName, new ArrayList<String>());
    }

    public static ArrayList<String> stopRecording(String pluginName) {
        ArrayList<String> result = pluginOutputHashMap.get(pluginName);
        Main.logDebugInfo("Stopping output recording of plugin " + pluginName);
        Main.logDebugInfo("Recorded " + result.size() + " entries.");
        pluginOutputHashMap.remove(pluginName);
        return result;
    }

    public static void handleLogRecord(Plugin plugin, LogRecord logRecord) {
        if (pluginOutputHashMap.containsKey(plugin.getName())) {
            pluginOutputHashMap.get(plugin.getName()).add(logRecord.getMessage());
        }
    }

	// unused useful functions
    public static void hijackSystemOutputStream() {
        PrintStream newOutput = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                Throwable t = new Throwable();
                super.println("Class " + t.getStackTrace()[1].getClassName() + " says: " + x);
            }
        };
        System.setOut(newOutput);
    }

    public static void hijackLogger(String loggerName) {
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                record.setMessage("Class " + record.getSourceClassName() + " says: " + record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        // May give problems with openJDK
        Logger.getLogger(loggerName).addHandler(handler);
    }
}

class WebsendPluginLoggerHandler extends Handler {

    Plugin plugin;

    WebsendPluginLoggerHandler(Plugin plugin) {
        this.plugin = plugin;
        Main.logDebugInfo("Tapped into: " + plugin.getName());
    }

    @Override
    public void publish(LogRecord record) {
        PluginOutputManager.handleLogRecord(plugin, record);
        if (Main.getSettings().isDebugMode()) {
            System.out.println("Catched log record from " + plugin.getName());
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
