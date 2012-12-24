package net.visualillusionsent.vibot.api.events;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

public abstract class InviteEvent extends BaseEvent {

    public InviteEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.INVITE);
    }

    public InviteEvent(BotPlugin plugin) {
        super(plugin, EventType.INVITE);
    }

    public abstract void execute(User user, Channel channel);

}
