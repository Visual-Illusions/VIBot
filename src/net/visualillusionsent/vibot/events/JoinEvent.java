package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class JoinEvent extends BaseEvent {

    public JoinEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.JOIN);
    }

    public abstract void execute(Channel channel, User user);
}
