package com.github.websend;

import com.github.websend.post.POSTHandlerThreadPool;
import com.github.websend.post.POSTRequest;
import com.github.websend.script.ScriptManager;
import com.github.websend.server.CommunicationServer;
import com.github.websend.server.NonSecureCommunicationServer;
import com.github.websend.server.SecureCommunicationServer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Settings settings;
    private static Logger logger;
    private static Server bukkitServer;
    private static Main plugin;
    private static File scriptsDir;
    private static int port;
    private static ScriptManager scriptManager;
    private static CommunicationServer server;
    private static POSTHandlerThreadPool requestThreadPool;

    @Override
    public void onEnable() {
        // Setup vars
        logger = this.getLogger();
        bukkitServer = this.getServer();
        plugin = this;
        port = this.getServer().getPort();

        // Parse config
        ConfigHandler configHandler = new ConfigHandler();
        try {
            settings = configHandler.loadSettings();
        } catch (FileNotFoundException ex) {
            configHandler.generateConfig();
            logger.info("Websend generated a config file. Go edit it!");
            return;
        } catch (IOException ex) {
            logger.info("Websend failed to read your configuration file.");
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        Main.logDebugInfo("Loading trusted hosts list.");
        try {
            File trustedFile = new File(this.getDataFolder(), "trusted.txt");
            if (!trustedFile.exists()) {
                TrustedHosts.writeDefaultFile(trustedFile);
            }
            TrustedHosts.load(trustedFile);
        } catch (IOException ex) {
            Main.logger.log(Level.SEVERE, null, ex);
        }

        
        //Setup SSL keystore
        if(settings.isSSLEnabled()){
            Main.logDebugInfo("Loading SSL settings.");
            if(settings.getSSLPassword() == null){
                Main.logger.log(Level.WARNING, "SSL password is not set in configuration. Connections are INSECURE.");
            }else{
                if(System.getProperty("javax.net.ssl.keyStore") != null){
                    Main.logger.log(Level.WARNING, "javax.net.ssl.keyStore is already set. "
                            + "Websend will override it, but this may introduce unexpected behaviour in other plugins.");
                }else if(System.getProperty("javax.net.ssl.keyStorePassword") != null){
                    Main.logger.log(Level.WARNING, "javax.net.ssl.keyStorePassword is already set. "
                            + "Websend will override it, but this may introduce unexpected behaviour in other plugins.");
                }
                try {
                    File certFile = new File(this.getDataFolder(), "websendKeyStore");
                    if(certFile.exists()){
                        System.setProperty("javax.net.ssl.keyStore", certFile.getCanonicalPath());
                        System.setProperty("javax.net.ssl.keyStorePassword", settings.getSSLPassword());
                    }else{
                        Main.logger.log(Level.WARNING, "No SSL keystore found. Connections are INSECURE.");
                    }
                } catch (IOException ex) {
                    Main.logger.log(Level.WARNING, "Failed to set SSL keystore path. Connections are INSECURE.", ex);
                    settings.setSSLEnabled(false);
                }
            }
        }
        
         //Setup webrequest thread pool
        Main.logDebugInfo("Loading POST handler pool.");
        requestThreadPool = new POSTHandlerThreadPool();
        
        // Setup scripts
        Main.logDebugInfo("Loading scripts.");
        scriptsDir = new File(this.getDataFolder(), "scripts");
        scriptManager = new ScriptManager();
        if (scriptsDir.exists()) {
            scriptManager.loadScripts();
        } else {
            if (!new File(scriptsDir, "compiled").mkdirs()) {
                Main.logger.log(Level.WARNING, "Failed to make scripts directory.");
            }
        }

        // Start server
        if (settings.isServerActive()) {
            if(settings.isSSLEnabled()){
                Main.logDebugInfo("Loading secure webrequest server on port "+settings.getPort()+".");
                server = new SecureCommunicationServer();
            }else{
                Main.logDebugInfo("Loading regular webrequest server on port "+settings.getPort()+".");
                server = new NonSecureCommunicationServer();
            }
            server.start();
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stopServer();
        }
    }

    public static void doCommand(String[] args, Player player) {
        URL url;
        try {
            url = new URL(Main.getSettings().getURL());
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "Failed to construct URL from config.", ex);
            return;
        }
        POSTRequest request = new POSTRequest(url, args, player, false);
        requestThreadPool.doRequest(request);
    }

    public static void doCommand(String[] args, String ply) {
        URL url;
        try {
            url = new URL(Main.getSettings().getURL());
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "Failed to construct URL from config.", ex);
            return;
        }
        POSTRequest request = new POSTRequest(url, args, ply, false);
        requestThreadPool.doRequest(request);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!this.isEnabled()){
            logger.log(Level.SEVERE, "Websend is disabled. Restart the server to run commands.");
        }else if (cmd.getName().equalsIgnoreCase("websend") || cmd.getName().equalsIgnoreCase("ws")) {
            URL url;
            try {
                url = new URL(Main.getSettings().getURL());
            } catch (MalformedURLException ex) {
               logger.log(Level.SEVERE, "Failed to construct URL from config.", ex);
                return true;
            }
            if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
                POSTRequest request = new POSTRequest(url, args, "@Console", false);
                requestThreadPool.doRequest(request);
                return true;
            } else if (sender instanceof Player) {
                Player plsender = (Player) sender;
                // try{
                if (plsender.hasPermission("websend")) {
                    if (args.length > 0) {
                        if (args[0].contains("-wp:")) {
                            if (plsender.isOp()) {
                                try {
                                    url = new URL(Main.getSettings().getURL());
                                } catch (MalformedURLException ex) {
                                    logger.log(Level.SEVERE, "Failed to construct URL from config.", ex);
                                    return true;
                                }
                            }
                        }
                    }
                    POSTRequest request = new POSTRequest(url, args, plsender, false);
                    requestThreadPool.doRequest(request);
                    return true;
                } else {
                    plsender.sendMessage("You are not allowed to use this command.");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("wsScripts")) {
            if (sender.hasPermission("websend.scripts")) {
                if (args.length < 1) {
                    sender.sendMessage(" /*/ Websend Scripts Menu /*/");
                    sender.sendMessage("    -wsScripts reload");
                    sender.sendMessage("       Reloads the scripts in the scripts folder.");
                    sender.sendMessage("    -wsScripts reload <scriptname>");
                    sender.sendMessage("       Reloads the script.");
                    sender.sendMessage("    -wsScripts list");
                    sender.sendMessage("       Lists the currently loaded scripts.");
                } else if (args[0].equals("reload")) {
                    if (args.length < 2) {
                        scriptManager.reload();
                    } else {
                        scriptManager.reload(args[1]);
                    }
                    sender.sendMessage("Reload complete.");
                } else if (args[0].equals("list")) {
                    sender.sendMessage("Currently loaded scripts:");
                    for (String string : scriptManager.getScriptNames()) {
                        sender.sendMessage("   -" + string);
                    }
                } else if (scriptManager.hasScript(args[0])) {
                    scriptManager.invokeScript(args[0]);
                } else {
                    sender.sendMessage("/wsscript " + args[0] + " does not exist.");
                }
            } else {
                sender.sendMessage("You are not allowed to use this command.('websend.scripts')");
            }
            return true;
        }
        return false;
    }

    public static Server getBukkitServer() {
        return bukkitServer;
    }

    public static Main getInstance() {
        return plugin;
    }

    public static int getPort() {
        return port;
    }

    public static ScriptManager getScriptManager() {
        return scriptManager;
    }

    public static File getScriptsDir() {
        return scriptsDir;
    }

    public static CommunicationServer getCommunicationServer() {
        return server;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static Logger getMainLogger() {
        return logger;
    }

    public static void logDebugInfo(String message) {
        logDebugInfo(Level.INFO, message);
    }

    public static void logDebugInfo(Level level, String message) {
        logDebugInfo(level, message, null);
    }

    public static void logDebugInfo(Level level, String message, Exception ex) {
        if (Main.getSettings() == null || Main.getSettings().isDebugMode()) {
            Main.getMainLogger().log(level, message, ex);
        }
    }
    
    public static void logError(String message, Exception ex) {
        Main.getMainLogger().log(Level.SEVERE, message, ex);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
