package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class NickChangeCommand extends BaseCommand {

    public NickChangeCommand() {
        super(null, new String[] { "nick" }, "!nick <newnick>", "Changes the bot's nickname", 2, 2, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        VIBot.changeNick(args[1]);
        return true;
    }
}
