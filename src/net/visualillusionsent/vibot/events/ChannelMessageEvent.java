package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class ChannelMessageEvent extends BaseEvent {

    public ChannelMessageEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.CHANNEL_MESSAGE);
    }

    public abstract void execute(Channel channel, User user, String message);

}
