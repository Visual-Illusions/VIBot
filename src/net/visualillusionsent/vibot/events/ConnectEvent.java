package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class ConnectEvent extends BaseEvent {

    public ConnectEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.CONNECT);
    }

    public abstract void execute();

}
