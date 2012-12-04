package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPluginLoader;

final class ListPluginsCommand extends BaseCommand {

    public ListPluginsCommand() {
        super(null, new String[] { "listplugins" }, "!listplugins", "Gives a list of plugins", 1, -1, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        channel.sendMessage(BotPluginLoader.getInstance().getPluginList());
        return true;
    }

}
