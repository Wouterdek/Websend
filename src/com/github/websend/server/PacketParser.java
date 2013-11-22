package com.github.websend.server;

import com.avaje.ebean.enhance.asm.Opcodes;
import com.github.websend.Main;
import com.github.websend.PluginOutputManager;
import com.github.websend.WebsendConsoleCommandSender;
import com.github.websend.WebsendPlayerCommandSender;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketParser {

    public static void parseDoCommandAsPlayer(DataInputStream in, DataOutputStream out) throws IOException {
        String command = readString(in);
        String playerStr = readString(in);
        Player player = Main.getBukkitServer().getPlayer(playerStr);

        if (player == null) {
            Main.logDebugInfo(Level.WARNING, "Can't execute command (" + command + ") as player: Player cannot be found (" + playerStr + ")");
            out.writeInt(0);
            out.flush();
            return;
        }

        boolean success;
        try {
            if (Main.getSettings().areCommandExecutorsWrapped()) {
                Plugin targetPlugin = Main.getBukkitServer().getPluginCommand(command).getPlugin();
                success = Main.getBukkitServer().dispatchCommand(new WebsendPlayerCommandSender(player, targetPlugin), command);
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

    public static void parseDoCommandAsConsole(DataInputStream in, DataOutputStream out) throws IOException {
        String command = readString(in);
        boolean success;
        try {
            //config check?
            if (Main.getSettings().areCommandExecutorsWrapped()) {
                Plugin targetPlugin = Main.getBukkitServer().getPluginCommand(command).getPlugin();
                success = Main.getBukkitServer().dispatchCommand(
                        new WebsendConsoleCommandSender(
                                Main.getBukkitServer().getConsoleSender(),
                                targetPlugin),
                        command);
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

    public static void parseDoScript(DataInputStream in, DataOutputStream out) throws IOException {
        String scriptName = readString(in);
        Main.getScriptManager().invokeScript(scriptName);
    }

    public static void parseWriteOutputToConsole(DataInputStream in, DataOutputStream out) throws IOException {
        String message = readString(in);
        Main.getMainLogger().info(message);
    }

    public static void parseWriteOutputToPlayer(DataInputStream in, DataOutputStream out) throws IOException {
        String message = readString(in);
        String playerStr = readString(in);
        Player player = Main.getInstance().getServer().getPlayer(playerStr);
        if (player != null) {
            out.writeInt(1);
            player.sendMessage(message);
        } else {
            out.writeInt(0);
        }
        out.flush();
    }

    public static void parseBroadcast(DataInputStream in, DataOutputStream out) throws IOException {
        String message = readString(in);
        Main.getBukkitServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static boolean parsePasswordPacket(DataInputStream in, DataOutputStream out) throws IOException {
        String inPass = readString(in);
        return inPass.equals(Main.getSettings().getPassword());
    }

    public static void parseStartPluginOutputRecording(DataInputStream in, DataOutputStream out) throws IOException {
        String pluginName = readString(in);
        PluginOutputManager.startRecording(pluginName);
    }

    public static void parseEndPluginOutputRecording(DataInputStream in, DataOutputStream out) throws IOException {
        String pluginName = readString(in);
        ArrayList<String> output = PluginOutputManager.stopRecording(pluginName);
        out.writeInt(output.size());
        for (String cur : output) {
            writeString(out, cur);
        }
        out.flush();
    }

    private static String readString(DataInputStream in) throws IOException {
        int stringSize = in.readInt();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < stringSize; i++) {
            buffer.append(in.readChar());
        }

        return buffer.toString();
    }

    private static void writeString(DataOutputStream out, String string) throws IOException {
        out.writeInt(string.length());
        out.writeChars(string);
    }
}
