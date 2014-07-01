package com.github.websend.server;

import com.github.websend.Main;
import com.github.websend.PluginOutputManager;
import com.github.websend.Util;
import com.github.websend.WebsendConsoleCommandSender;
import com.github.websend.WebsendPlayerCommandSender;
import com.github.websend.server.remotejava.InvokeRequest;
import com.github.websend.server.remotejava.MethodRequest;
import com.github.websend.server.remotejava.MiscRequest;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketParser {
    private CommunicationServer server;
    public PacketParser(CommunicationServer server) {
        this.server = server;
    }
    
    boolean parseAuthenticationRequestPacket(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String header = in.readString();
        if(!header.equals("websendmagic")){
            return false;
        }
        
        SecureRandom random = new SecureRandom();
        int seed = random.nextInt();
        out.writeInt(seed);
        String correctHash = Util.hash(seed+Main.getSettings().getPassword());
        
        String authString = in.readString();
        boolean success = authString.equals(correctHash);
        out.writeInt(success?1:0);
        return success;
    }
    
    void parseDoCommandAsPlayer(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String command = in.readString();
        String playerStr = in.readString();
        Player player = Main.getBukkitServer().getPlayerExact(playerStr);

        if (player == null) {
            Main.logDebugInfo(Level.WARNING, "Can't execute command (" + command + ") as player: Player cannot be found (" + playerStr + ")");
            out.writeInt(0);
            out.flush();
            return;
        }

        boolean success;
        try {
            if (Main.getSettings().areCommandExecutorsWrapped()) {
                PluginCommand pluginCommand = Main.getBukkitServer().getPluginCommand(command);
                if(pluginCommand != null){
                    Plugin targetPlugin = pluginCommand.getPlugin();
                    success = Main.getBukkitServer().dispatchCommand(new WebsendPlayerCommandSender(player, targetPlugin), command);
                }else{
                    Main.getMainLogger().log(Level.WARNING, "Cannot execute command '"+command+"': Command does not exist.");
                    success = false;
                }
            } else {
                success = Main.getBukkitServer().dispatchCommand(player, command);
            }
        } catch (Exception ex) {
            Main.logDebugInfo(Level.WARNING, "Websend caught an exception while running command '" + command + "'", ex);
            success = false;
        }

        if (success) {
            out.writeInt(1);
        } else {
            out.writeInt(0);
        }
        out.flush();
    }

    void parseDoCommandAsConsole(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String command = in.readString();
        boolean success;
        try {
            //config check?
            if (Main.getSettings().areCommandExecutorsWrapped()) {
                PluginCommand pluginCommand = Main.getBukkitServer().getPluginCommand(command);
                if(pluginCommand != null){
                    Plugin targetPlugin = pluginCommand.getPlugin();
                    success = Main.getBukkitServer().dispatchCommand(
                        new WebsendConsoleCommandSender(
                                Main.getBukkitServer().getConsoleSender(),
                                targetPlugin),
                                command);
                }else{
                    Main.getMainLogger().log(Level.WARNING, "Cannot execute command '"+command+"': Command does not exist.");
                    success = false;
                }
            } else {
                success = Main.getBukkitServer().dispatchCommand(Main.getBukkitServer().getConsoleSender(), command);
            }
        } catch (Exception ex) {
            Main.logDebugInfo(Level.WARNING, "Websend caught an exception while running command '" + command + "'", ex);
            success = false;
        }
        if (success) {
            out.writeInt(1);
        } else {
            out.writeInt(0);
        }
        out.flush();
    }

    void parseDoScript(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String scriptName = in.readString();
        Main.getScriptManager().invokeScript(scriptName);
    }

    void parseWriteOutputToConsole(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String message = in.readString();
        Main.getMainLogger().info(message);
    }

    void parseWriteOutputToPlayer(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String message = in.readString();
        String playerStr = in.readString();
        Player player = Main.getInstance().getServer().getPlayerExact(playerStr);
        if (player != null) {
            out.writeInt(1);
            player.sendMessage(message);
        } else {
            out.writeInt(0);
        }
        out.flush();
    }

    void parseBroadcast(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String message = in.readString();
        Main.getBukkitServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    void parseStartPluginOutputRecording(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String pluginName = in.readString();
        PluginOutputManager.startRecording(pluginName);
    }

    void parseEndPluginOutputRecording(ComplexInputStream in, ComplexOutputStream out) throws IOException {
        String pluginName = in.readString();
        ArrayList<String> output = PluginOutputManager.stopRecording(pluginName);
        out.writeInt(output.size());
        for (String cur : output) {
            out.writeString(cur);
        }
        out.flush();
    }
    
    static final int RJ_OPEN_SESSION            = 30;
    static final int RJ_CLOSE_SESSION           = 31;
    static final int RJ_REQUEST_METHOD_BY_OBJ   = 32;
    static final int RJ_REQUEST_METHOD_BY_CLASS = 33;
    static final int RJ_REQUEST_INVOKE          = 34;
    static final int RJ_REQUEST_INVOKE_STATIC   = 35;
    static final int RJ_REQUEST_RELEASE_OBJECT  = 36;
    static final int RJ_REQUEST_RELEASE_METHOD  = 37;
    void parseRemoteJavaRequest(byte packetHeader, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        if(packetHeader == RJ_OPEN_SESSION){
            Main.logDebugInfo("Opening remote java session.");
            MiscRequest.openSession(server, in, out);
        }else if(packetHeader == RJ_CLOSE_SESSION){
            Main.logDebugInfo("Closing remote java session.");
            MiscRequest.closeSession(server, in, out);
        }else if(packetHeader == RJ_REQUEST_METHOD_BY_OBJ){
            Main.logDebugInfo("Received method by object request");
            MethodRequest.byObject(server, in, out);
        }else if(packetHeader == RJ_REQUEST_METHOD_BY_CLASS){
            Main.logDebugInfo("Received method by class request");
            MethodRequest.byClass(server, in, out);
        }else if(packetHeader == RJ_REQUEST_INVOKE){
            Main.logDebugInfo("Received method invoke request");
            InvokeRequest.onObject(server, in, out);
        }else if(packetHeader == RJ_REQUEST_INVOKE_STATIC){
            Main.logDebugInfo("Received static method invoke request");
            InvokeRequest.onClass(server, in, out);
        }else if(packetHeader == RJ_REQUEST_RELEASE_OBJECT){
            Main.logDebugInfo("Received object release request");
            MiscRequest.releaseObject(server, in, out);
        }else if(packetHeader == RJ_REQUEST_RELEASE_METHOD){
            Main.logDebugInfo("Received method release request");
            MiscRequest.releaseMethod(server, in, out);
        }
    }
}
