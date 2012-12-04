package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

final class ReloadPluginCommand extends BaseCommand {

    public ReloadPluginCommand() {
        super(null, new String[] { "reloadplugin" }, "!reloadplugin <plugin>", "Reloads a plugin", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String message = BotPluginLoader.getInstance().reloadPlugin(args[1]) ? "Reloaded plugin successfully!" : "An exception occured while reloading plugin...";
        channel.sendMessage(user.getNick().concat(", ").concat(message));
        return true;
    }
}
