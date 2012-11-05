package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "reloadplugin" }, usage = "!reloadplugin <plugin>", desc = "Reloads a plugin", adminonly = true)
final class ReloadPluginCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else {
            String message = Misc.getLoader().reloadPlugin(args[1]) ? "Reloaded plugin successfully!" : "An exception occured while reloading plugin...";
            channel.sendMessage(user.getNick() + ", " + message);
        }
        return true;
    }
}
