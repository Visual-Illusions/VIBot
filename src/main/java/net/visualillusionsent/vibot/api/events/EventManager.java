/* 
 * Copyright 2012 Visual Illusions Entertainment.
 *  
 * This file is part of VIBot.
 *
 * VIBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIBot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with VIUtils.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.api.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.dcc.DccChat;
import net.visualillusionsent.vibot.io.dcc.DccFileTransfer;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Event Manager System
 * <p>
 * Handles registering and executing Events
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public class EventManager {
    /**
     * The {@link EventPriorityComparator} instance
     */
    private final EventPriorityComparator epc = new EventPriorityComparator();

    /**
     * The map of registered events
     */
    private final HashMap<EventType, List<BaseEvent>> registeredEvents;

    /**
     * The {@code EventManager} instance
     */
    private static EventManager instance;

    static {
        instance = new EventManager();
    }

    /**
     * Constructs a new {@code EventManager} object
     */
    private EventManager() {
        if (instance != null) {
            throw new IllegalStateException("Only one EventManager instance may be created at a time.");
        }
        registeredEvents = new HashMap<EventType, List<BaseEvent>>();
        for (EventType type : EventType.values()) {
            registeredEvents.put(type, new ArrayList<BaseEvent>());
        }
    }

    /**
     * Adds a new {@link BaseEvent}
     * 
     * @param event
     *            the {@link BaseEvent} to be added
     */
    public static final void addEvent(BaseEvent event) {
        instance.add(event);
    }

    /**
     * Adds a new {@link BaseEvent}
     * 
     * @param event
     *            the {@link BaseEvent} to be added
     */
    private final void add(BaseEvent event) {
        registeredEvents.get(event.getType()).add(event);
        Collections.sort(registeredEvents.get(event.getType()), epc);
    }

    /**
     * Removes a {@link BaseEvent}
     * 
     * @param event
     *            the {@link BaseEvent} to be removed
     */
    public static final void removeEvent(BaseEvent event) {
        instance.remove(event);
    }

    /**
     * Removes a {@link BaseEvent}
     * 
     * @param event
     *            the {@link BaseEvent} to be removed
     */
    private final void remove(BaseEvent event) {
        registeredEvents.get(event.getType()).remove(event);
    }

    /**
     * Removes a {@link BotPlugin}'s registered {@link BaseEvent}s
     * 
     * @param plugin
     *            the {@link BotPlugin} to have events removed
     */
    public static final void unregisterPluginHooks(BotPlugin plugin) {
        instance.removePluginHooks(plugin);
    }

    /**
     * Removes a {@link BotPlugin}'s registered {@link BaseEvent}s
     * 
     * @param plugin
     *            the {@link BotPlugin} to have events removed
     */
    private final void removePluginHooks(BotPlugin plugin) {
        synchronized (registeredEvents) {
            List<BaseEvent> tempList = new ArrayList<BaseEvent>();
            for (EventType type : EventType.values()) {
                List<BaseEvent> events = registeredEvents.get(type);
                for (BaseEvent event : events) {
                    if (event.getClass().equals(plugin)) {
                        tempList.add(event);
                    }
                }
            }
            for (BaseEvent event : tempList) {
                removeEvent(event);
            }
        }
    }

    /**
     * Called when the {@link ChannelMessageEvent} is activated
     * 
     * @param channel
     *            the {@link Channel} initiated from
     * @param user
     *            the {@link User} initiating the event
     * @param msg
     *            the message being sent to channel
     */
    public static final void activateChannelMessageEvent(Channel channel, User user, String msg) {
        instance.dispatchChannelMessageEvent(channel, user, msg);
    }

    /**
     * Dispatches the {@link ChannelMessageEvent}
     * 
     * @param channel
     *            the {@link Channel} initiated from
     * @param user
     *            the {@link User} initiating the event
     * @param msg
     *            the message being sent to channel
     */
    private final void dispatchChannelMessageEvent(Channel channel, User user, String msg) {
        synchronized (registeredEvents) {
            for (BaseEvent chanMessageEvent : registeredEvents.get(EventType.CHANNEL_MESSAGE)) {
                try {
                    ((ChannelMessageEvent) chanMessageEvent).execute(channel, user, msg);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'ChannelMessageEvent' for Plugin: ".concat(chanMessageEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link ConnectEvent} is activated
     */
    public static final void activateConnectEvent() {
        instance.dispatchConnectEvent();
    }

    /**
     * Dispatches the {@link ConnectEvent}
     */
    private void dispatchConnectEvent() {
        synchronized (registeredEvents) {
            for (BaseEvent connectEvent : registeredEvents.get(EventType.CONNECT)) {
                try {
                    ((ConnectEvent) connectEvent).execute();
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'ConnectEvent' for Plugin: ".concat(connectEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link FileTransferFinishedEvent} is activated
     * 
     * @param transfer
     *            the {@link DccFileTransfer} object
     * @param ex
     *            the {@link Exception} thrown if one occured
     */
    public static final void activateFileTransferFinishedEvent(DccFileTransfer transfer, Exception ex) {
        instance.dispatchFileTransferFinishedEvent(transfer, ex);
    }

    /**
     * Dispatches the {@link FileTransferFinishedEvent}
     * 
     * @param transfer
     *            the {@link DccFileTransfer} object
     * @param ex
     *            the {@link Exception} thrown if one occured
     */
    private final void dispatchFileTransferFinishedEvent(DccFileTransfer transfer, Exception ex) {
        synchronized (registeredEvents) {
            for (BaseEvent fileTransferFinishedEvent : registeredEvents.get(EventType.FILE_TRANSFER_FINISHED)) {
                try {
                    ((FileTransferFinishedEvent) fileTransferFinishedEvent).execute(transfer, ex);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'FileTransferFinishedEvent' for Plugin: ".concat(fileTransferFinishedEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link IncomingChatRequestEvent} is activated
     * 
     * @param chat
     *            the {@link DccChat} object
     */
    public static final void activateIncomingChatRequestEvent(DccChat chat) {
        instance.dispatchIncomingChatRequestEvent(chat);
    }

    /**
     * Dispatches the {@link IncomingChatRequestEvent}
     * 
     * @param chat
     *            the {@link DccChat} object
     */
    private final void dispatchIncomingChatRequestEvent(DccChat chat) {
        synchronized (registeredEvents) {
            for (BaseEvent incomingChatRequestEvent : registeredEvents.get(EventType.INCOMING_CHAT_REQUEST)) {
                try {
                    ((IncomingChatRequestEvent) incomingChatRequestEvent).execute(chat);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'IncomingChatRequestEvent' for Plugin: ".concat(incomingChatRequestEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link IncomingFileTransferEvent} is activated
     * 
     * @param transfer
     *            the {@link DccFileTransfer} object
     */
    public static final void activateIncomingFileTransferEvent(DccFileTransfer transfer) {
        instance.dispatchIncomingFileTransferEvent(transfer);
    }

    /**
     * Dispatches the {@link IncomingFileTransferEvent}
     * 
     * @param transfer
     *            the {@link DccFileTransfer} object
     */
    private final void dispatchIncomingFileTransferEvent(DccFileTransfer transfer) {
        synchronized (registeredEvents) {
            for (BaseEvent incomingFileTransferEvent : registeredEvents.get(EventType.INCOMING_FILE_TRANSFER)) {
                try {
                    ((IncomingFileTransferEvent) incomingFileTransferEvent).execute(transfer);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'IncomingFileTransferEvent' for Plugin: ".concat(incomingFileTransferEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link InviteEvent} is activated
     * 
     * @param user
     *            the {@link User} um... something
     */
    public static final void activateInviteEvent(User user, Channel channel) {
        instance.dispatchInviteEvent(user, channel);
    }

    private final void dispatchInviteEvent(User user, Channel channel) {
        synchronized (registeredEvents) {
            for (BaseEvent inviteEvent : registeredEvents.get(EventType.INVITE)) {
                try {
                    ((InviteEvent) inviteEvent).execute(user, channel);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'InviteEvent' for Plugin: ".concat(inviteEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link KickEvent} is activated
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
    public static final void activateKickEvent(Channel channel, User kicked, User kicker, String reason) {
        instance.dispatchKickEvent(channel, kicked, kicker, reason);
    }

    /**
     * Dispatches the {@link KickEvent}
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
    private final void dispatchKickEvent(Channel channel, User kicked, User kicker, String reason) {
        synchronized (registeredEvents) {
            for (BaseEvent kickEvent : registeredEvents.get(EventType.KICK)) {
                try {
                    ((KickEvent) kickEvent).execute(channel, kicked, kicker, reason);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'KickEvent' for Plugin: ".concat(kickEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link JoinEvent} is activated
     * 
     * @param channel
     *            the {@link Channel} being joined
     * @param user
     *            the {@link User} joining the {@link Channel}
     */
    public static final void activateJoinEvent(Channel channel, User user) {
        instance.dispatchJoinEvent(channel, user);
    }

    /**
     * Dispatch the {@link JoinEvent}
     * 
     * @param channel
     *            the {@link Channel} being joined
     * @param user
     *            the {@link User} joining the {@link Channel}
     */
    private final void dispatchJoinEvent(Channel channel, User user) {
        synchronized (registeredEvents) {
            for (BaseEvent joinEvent : registeredEvents.get(EventType.JOIN)) {
                try {
                    ((JoinEvent) joinEvent).execute(channel, user);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'JoinEvent' for Plugin: ".concat(joinEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link PartEvent} is activated
     * 
     * @param channel
     *            the {@link Channel} be left
     * @param user
     *            the {@link User} leaving
     */
    public static final void activatePartEvent(Channel channel, User user) {
        instance.dispatchPartEvent(channel, user);
    }

    /**
     * Dispatches the {@link PartEvent}
     * 
     * @param channel
     *            the {@link Channel} be left
     * @param user
     *            the {@link User} leaving
     */
    private final void dispatchPartEvent(Channel channel, User user) {
        synchronized (registeredEvents) {
            for (BaseEvent partEvent : registeredEvents.get(EventType.PART)) {
                try {
                    ((PartEvent) partEvent).execute(channel, user);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'PartEvent' for Plugin: ".concat(partEvent.getPlugin().getName()), e);
                }
            }
        }
    }

    /**
     * Called when the {@link PrivateMessageEvent} is activated
     * 
     * @param user
     *            the {@link User} sending the message
     * @param msg
     *            the message
     */
    public static final void activatePrivateMessageEvent(User user, String msg) {
        instance.dispatchPrivateMessageEvent(user, msg);
    }

    /**
     * Dispatches the {@link PrivateMessageEvent} is activated
     * 
     * @param user
     *            the {@link User} sending the message
     * @param msg
     *            the message
     */
    private final void dispatchPrivateMessageEvent(User user, String msg) {
        synchronized (registeredEvents) {
            for (BaseEvent privMessageEvent : registeredEvents.get(EventType.PRIVATE_MESSAGE)) {
                try {
                    ((PrivateMessageEvent) privMessageEvent).execute(user, msg);
                }
                catch (Exception e) {
                    BotLogMan.warning("Unhandled Exception caught while calling 'PrivateMessageEvent' for Plugin: ".concat(privMessageEvent.getPlugin().getName()), e);
                }
            }
        }
    }
}
