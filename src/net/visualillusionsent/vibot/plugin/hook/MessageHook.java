package net.visualillusionsent.vibot.plugin.hook;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class MessageHook extends BaseHook {

    public MessageHook(BotPlugin plugin) {
        super(plugin, HookType.MESSAGE);
    }

    public abstract void execute(Channel channel, User user, String message);

}
