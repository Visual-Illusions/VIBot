package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "nick" }, usage = "!nick <newnick>", desc = "Changes the bot's nickname", adminonly = true)
final class NickChangeCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("You didn't specify a new nick!");
        } else {
            Misc.changeNick(args[1]);
        }
        return true;
    }
}
