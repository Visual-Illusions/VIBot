package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;

final class DisconnectCommand extends BaseCommand {

    public DisconnectCommand() {
        super(null, new String[] { "disconnect" }, "!disconnect", "If user is Admin/Console, Bot shut down", 1, -1, false, false, true);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        VIBot.terminate(0);
        return true;
    }
}
