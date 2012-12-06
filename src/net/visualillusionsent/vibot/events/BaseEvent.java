package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class BaseEvent {
    private final BotPlugin plugin;
    private final EventType type;
    private final EventPriority priority;

    public BaseEvent(BotPlugin plugin, EventPriority priority, EventType type) {
        this.plugin = plugin;
        this.type = type;
        this.priority = priority;
        EventManager.addEvent(this);
    }

    public BotPlugin getPlugin() {
        return plugin;
    }

    public EventType getType() {
        return type;
    }

    public EventPriority getPriority() {
        return priority;
    }
}
