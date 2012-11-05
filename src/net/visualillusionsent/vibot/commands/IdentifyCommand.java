package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "identify" }, usage = "!identify", desc = "Tells the bot to identify with NickServ", adminonly = true)
final class IdentifyCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        Misc.identify();
        return true;
    }
}
