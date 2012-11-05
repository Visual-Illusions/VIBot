package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "stfu" }, usage = "!stfu", desc = "Quiets the Bot in the channel", adminonly = true)
final class ShutTheFuckUpCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        Misc.Mute(channel.getName());
        return true;
    }
}
