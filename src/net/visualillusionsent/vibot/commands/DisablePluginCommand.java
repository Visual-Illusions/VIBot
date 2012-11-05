package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "disableplugin" }, usage = "!disableplugin <plugin>", desc = "Disables a plugin", adminonly = true)
final class DisablePluginCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!argCheck(2, args)) {
            user.sendMessage("Usage: " + this.getClass().getAnnotation(BotCommand.class).usage());
        } else {
            Misc.getLoader().disablePlugin(args[1]);
            channel.sendMessage("Plugin disabled!");
        }
        return true;
    }
}
