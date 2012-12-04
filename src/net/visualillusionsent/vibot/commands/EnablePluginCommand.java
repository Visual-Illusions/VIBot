package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

final class EnablePluginCommand extends BaseCommand {

    public EnablePluginCommand() {
        super(null, new String[] { "enableplugin" }, "!enableplugin <plugin>", "Enables a plugin", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String message = BotPluginLoader.getInstance().enablePlugin(args[1]) ? "Enabled plugin successfully!" : "An exception occured while enabling plugin...";
        channel.sendMessage(message);
        return true;
    }
}
