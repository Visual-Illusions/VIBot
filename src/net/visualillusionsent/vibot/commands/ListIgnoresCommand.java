package net.visualillusionsent.vibot.commands;

import java.util.ArrayList;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "listignore" }, usage = "!listignore", desc = "List of users being ignored in the channel", oponly = true)
final class ListIgnoresCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        ArrayList<String> ignore = Misc.getIgnoreList(channel.getName());
        if (!ignore.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String igno : ignore) {
                sb.append(igno + ", ");
            }
            channel.sendMessage("I am ignoring: " + sb.toString());
        } else {
            channel.sendMessage("I'm not ignoring anyone...");
        }
        return true;
    }
}
