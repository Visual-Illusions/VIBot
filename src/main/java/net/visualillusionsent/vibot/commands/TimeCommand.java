package net.visualillusionsent.vibot.commands;

import java.util.TimeZone;

import net.visualillusionsent.utils.DateUtils;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

@BotCommand(main = "time", usage = "!time [TimeZone]", desc = "Shows the current time", maxParam = 2)
public final class TimeCommand extends BaseCommand {
    private final String print = "The current time in TimeZone: '%s' is %s";

    public TimeCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final synchronized boolean execute(Channel channel, User user, String[] args) {
        long current = System.currentTimeMillis();
        if (args.length > 1) {
            TimeZone zone = TimeZone.getTimeZone(args[1]);
            if (channel != null) {
                channel.sendMessage(String.format(print, zone.getID(), DateUtils.longToTimeDate(current, zone), ""));
            }
            else {
                user.sendMessage(String.format(print, zone.getID(), DateUtils.longToTimeDate(current, zone), ""));
            }
        }
        else {
            if (channel != null) {
                channel.sendMessage("The current time where I am is : " + DateUtils.longToTimeDate(current));
            }
            else {
                user.sendMessage("The current time where I am is : " + DateUtils.longToTimeDate(current));
            }
        }
        return true;
    }
}
