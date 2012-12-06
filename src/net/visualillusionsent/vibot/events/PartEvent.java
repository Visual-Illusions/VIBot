package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class PartEvent extends BaseEvent {

    public PartEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.PART);
    }

    public abstract void execute(Channel channel, User user);

}
