package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class EchoCommand extends BaseCommand {

    public EchoCommand() {
        super(null, new String[] { "echo" }, "!echo <message>", "Echoes the message", 2, -1, true, false, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        String message = "";
        try {
            message = StringUtils.joinString(args, " ", 1);
        }
        catch (UtilityException e) {
            message = e.getMessage(); //This shouldn't ever happen but you never know...
        }
        channel.sendMessage(message);
        return true;
    }
}
