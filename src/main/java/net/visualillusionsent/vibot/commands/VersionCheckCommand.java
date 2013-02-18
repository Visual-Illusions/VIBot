package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

@BotCommand(main = "version", usage = "!version [pluginname]", desc = "Checks the version of the VIBot or specified plugin", maxParam = 2, op = true)
public final class VersionCheckCommand extends BaseCommand {

    public VersionCheckCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        if (args.length > 1) {
            BotPlugin plugin = BotPluginLoader.getBotPlugin(args[1]);
            String message = "Plugin not found!";
            if (plugin != null) {
                if (!plugin.supportsversionChecker()) {
                    message = "Plugin does not currently support Version Checking...";
                }
                else {
                    Boolean isLatest = plugin.isLatestVersion();
                    message = plugin.getName() + " v" + plugin.getVersion() + " | " + (isLatest == null ? "An Error occured while checking version..." : plugin.getUpdateMessage());
                }
            }

            if (channel != null) {
                channel.sendMessage(message);
            }
            else {
                user.sendMessage(message);
            }
        }
        else {
            if (channel != null) {
                channel.sendMessage("VIBot v." + VIBot.getBotVersionBuild());
            }
            else {
                user.sendMessage("VIBot v." + VIBot.getBotVersionBuild());
            }
        }
        return true;
    }
}
