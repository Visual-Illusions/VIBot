package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "echo" }, usage = "!echo <message>", desc = "Echoes the message")
final class EchoCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else {
            String message = Misc.combineSplit(1, args, " ");
            channel.sendMessage(message);
        }
        return true;
    }
}
