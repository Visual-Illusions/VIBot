package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBotMain;

@BotCommand(aliases = { "disconnect" }, usage = "!disconnect", desc = "If user is Admin/Console, Bot shut down", adminonly = true)
final class DisconnectCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        VIBotMain.terminate();
        return true;
    }
}
