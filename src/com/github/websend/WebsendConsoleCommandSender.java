package com.github.websend;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class WebsendConsoleCommandSender implements ConsoleCommandSender {
    /* This class allows tapping into command output from plugins
     * if the output is sent through the commandsender.
     *
     * Tap this method(1.6.4): sendRawMessage
     */

    private final ConsoleCommandSender parent;
    private final Plugin commandTargetPlugin;

    public WebsendConsoleCommandSender(ConsoleCommandSender parent, Plugin commandTargetPlugin) {
        this.parent = parent;
        this.commandTargetPlugin = commandTargetPlugin;
    }

    @Override
    public void sendMessage(java.lang.String param0) {
        PluginOutputManager.handleLogRecord(commandTargetPlugin, new LogRecord(Level.INFO, param0));
        parent.sendMessage(param0);
    }

    @Override
    public void sendMessage(java.lang.String[] param0) {
        for (String str : param0) {
            PluginOutputManager.handleLogRecord(commandTargetPlugin, new LogRecord(Level.INFO, str));
        }
        parent.sendMessage(param0);
    }

    @Override
    public void sendRawMessage(java.lang.String param0) {
        PluginOutputManager.handleLogRecord(commandTargetPlugin, new LogRecord(Level.INFO, param0));
        parent.sendRawMessage(param0);
    }

    @Override
    public Server getServer() {
        return parent.getServer();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public boolean isPermissionSet(String string) {
        return parent.isPermissionSet(string);
    }

    @Override
    public boolean isPermissionSet(Permission prmsn) {
        return parent.isPermissionSet(prmsn);
    }

    @Override
    public boolean hasPermission(String string) {
        return parent.hasPermission(string);
    }

    @Override
    public boolean hasPermission(Permission prmsn) {
        return parent.hasPermission(prmsn);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        return parent.addAttachment(plugin, string, bln);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return parent.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        return parent.addAttachment(plugin, string, bln, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return parent.addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment pa) {
        parent.removeAttachment(pa);
    }

    @Override
    public void recalculatePermissions() {
        parent.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return parent.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return parent.isOp();
    }

    @Override
    public void setOp(boolean bln) {
        parent.setOp(bln);
    }

    @Override
    public boolean isConversing() {
        return parent.isConversing();
    }

    @Override
    public void acceptConversationInput(String string) {
        parent.acceptConversationInput(string);
    }

    @Override
    public boolean beginConversation(Conversation c) {
        return parent.beginConversation(c);
    }

    @Override
    public void abandonConversation(Conversation c) {
        parent.abandonConversation(c);
    }

    @Override
    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae) {
        parent.abandonConversation(c, cae);
    }
}
