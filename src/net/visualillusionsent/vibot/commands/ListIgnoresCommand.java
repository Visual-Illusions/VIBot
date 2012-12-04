package net.visualillusionsent.vibot.commands;

import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

final class ListIgnoresCommand extends BaseCommand {

    public ListIgnoresCommand() {
        super(null, new String[] { "listignore" }, "!listignore", "List of users being ignored in the channel", 1, -1, false, true, false);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        List<User> ignore = channel.getIgnoreList();
        if (!ignore.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (User igno : ignore) {
                sb.append(igno.getNick());
                sb.append(", ");
            }
            channel.sendMessage("I am ignoring: ".concat(sb.toString().trim()));
        }
        else {
            channel.sendMessage("I'm not ignoring anyone...");
        }
        return true;
    }
}
