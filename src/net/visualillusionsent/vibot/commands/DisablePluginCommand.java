package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

final class DisablePluginCommand extends BaseCommand {

    public DisablePluginCommand() {
        super(null, new String[] { "disableplugin" }, "!disableplugin <plugin>", "Disables a plugin", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        BotPluginLoader.getInstance().disablePlugin(args[1]);
        channel.sendMessage("Plugin disabled!");
        return true;
    }
}
