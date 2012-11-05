package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "enableplugin" }, usage = "!enableplugin <plugin>", desc = "Enables a plugin", adminonly = true)
final class EnablePluginCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else {
            String message = Misc.getLoader().enablePlugin(args[1]) ? "Enabled plugin successfully!" : "An exception occured while enabling plugin...";
            channel.sendMessage(message);
        }
        return true;
    }
}
