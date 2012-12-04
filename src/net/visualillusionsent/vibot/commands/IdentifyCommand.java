package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class IdentifyCommand extends BaseCommand {

    public IdentifyCommand() {
        super(null, new String[] { "identify" }, "!identify", "Tells the bot to identify with NickServ", 1, -1, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        VIBot.identify();
        return true;
    }
}
