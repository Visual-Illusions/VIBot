package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "listplugins" }, usage = "!listplugins", desc = "Gives a list of plugins", oponly = true)
final class ListPluginsCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        channel.sendMessage(Misc.getLoader().getPluginList());
        return true;
    }

}
