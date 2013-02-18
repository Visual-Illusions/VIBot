package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.utils.UpdateException;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.api.plugin.BotPluginLoader;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

@BotCommand(main = "updateplugin", usage = "!updateplugiun <pluginname>", desc = "Attemps to update a Plugin, if the plugin supports VIUtils.Updater", minParam = 1, owner = true)
public final class UpdatePluginCommand extends BaseCommand {

    public UpdatePluginCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        BotPlugin plugin = BotPluginLoader.getBotPlugin(args[1]);
        if (plugin != null) {
            try {
                if (plugin.runUpdate()) {
                    message(channel, user, "Update Successful!");
                }
                else {
                    message(channel, user, "Update failed for unknown reasons...");
                }
            }
            catch (UpdateException ue) {
                message(channel, user, ue.getMessage());
            }
        }
        return true;
    }

    private final void message(Channel channel, User user, String message) {
        if (channel != null) {
            channel.sendMessage(message);
        }
        else {
            user.sendNotice(message);
        }
    }
}
