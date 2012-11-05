package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "ping" }, usage = "!ping", desc = "Sends a Pong")
final class PingCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        channel.sendMessage("| . :  |");
        return true;
    }

}
