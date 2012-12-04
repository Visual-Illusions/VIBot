package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class ShutTheFuckUpCommand extends BaseCommand {

    public ShutTheFuckUpCommand() {
        super(null, new String[] { "stfu" }, "!stfu", "Quiets the Bot in the channel", 1, -1, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!channel.isMuted()) {
            channel.toggleMute();
        }
        return true;
    }
}
