package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class OkThanksCommand extends BaseCommand {

    public OkThanksCommand() {
        super(null, new String[] { "kthx" }, "!kthx", "Un-Quites the Bot", 1, -1, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (channel.isMuted()) {
            channel.toggleMute();
        }
        return true;
    }
}
