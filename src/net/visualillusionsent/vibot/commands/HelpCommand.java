package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "help" }, usage = "!help", desc = "Displays a list of commands and their usage")
final class HelpCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        user.sendNotice("-- Help List for you for Channel: " + channel.getName() + " --");
        BaseCommand[] help = CommandParser.getInstance().getCommands();
        for (BaseCommand cmd : help) {
            BotCommand botcmd = cmd.getClass().getAnnotation(BotCommand.class);
            if (botcmd.adminonly() && !user.isAdmin()) {
                continue;
            }
            if (botcmd.oponly() && !(user.isAdmin() || user.isOp())) {
                continue;
            }
            user.sendNotice(botcmd.usage() + " - " + botcmd.desc());
        }
        return true;
    }

}
