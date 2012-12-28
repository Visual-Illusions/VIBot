package net.visualillusionsent.vibot.api.events;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

public abstract class KickEvent extends BaseEvent {

    /**
     * Constructs a new {@code KickEvent}<br>
     * <b>NOTE:</b>With this constructor, {@link EventPriority} will be read from the plugin.cfg file
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with this event
     */
    public KickEvent(BotPlugin plugin) {
        super(plugin, EventType.KICK);
    }

    /**
     * Constructs a new {@code KickEvent}
     * 
     * @param plugin
     *            the {@link BotPlugin} associated with this event
     * @param priority
     *            the {@link EventPriority} for the event
     */
    public KickEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.KICK);
    }

    /**
     * Event executor<br>
     * This is where the code should go for extending classes to handle the {@code KickEvent}
     * 
     * @param channel
     *            the {@link Channel} the kick happened in
     * @param kicked
     *            the {@link User} being kicked
     * @param kicker
     *            the {@link User} kicking the other
     * @param reason
     *            the reason for the kick
     */
    public abstract void execute(Channel channel, User kicked, User kicker, String reason);

}
