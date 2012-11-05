package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "kthx" }, usage = "!kthx", desc = "Un-Quites the Bot", adminonly = true)
final class OkThanksCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        Misc.unMute(channel.getName());
        return true;
    }
}
