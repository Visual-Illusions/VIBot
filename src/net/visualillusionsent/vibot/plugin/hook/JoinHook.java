package net.visualillusionsent.vibot.plugin.hook;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class JoinHook extends BaseHook {

    public JoinHook(BotPlugin plugin) {
        super(plugin);
        HookManager.addHook(this);
    }

    public abstract void execute(Channel channel, User user);

}
