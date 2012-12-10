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
package net.visualillusionsent.vibot.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.io.DccChat;
import net.visualillusionsent.vibot.io.DccFileTransfer;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public class EventManager {
    private final EventPriorityComparator epc = new EventPriorityComparator();
    private static EventManager instance;
    private HashMap<EventType, List<BaseEvent>> registeredEvents = new HashMap<EventType, List<BaseEvent>>();

    private EventManager() {
        for (EventType type : EventType.values()) {
            registeredEvents.put(type, new ArrayList<BaseEvent>());
        }
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public static void addEvent(BaseEvent event) {
        getInstance().add(event);
    }

    public static void removeEvent(BaseEvent event) {
        getInstance().remove(event);
    }

    private void add(BaseEvent event) {
        registeredEvents.get(event.getType()).add(event);
        Collections.sort(registeredEvents.get(event.getType()), epc);
    }

    private void remove(BaseEvent event) {
        registeredEvents.get(event.getType()).remove(event);
    }

    public void removePluginHooks(BotPlugin plugin) {
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

    public void callChannelMessageEvent(Channel channel, User user, String msg) {
        synchronized (registeredEvents) {
            for (BaseEvent chanMessageEvent : registeredEvents.get(EventType.CHANNEL_MESSAGE)) {
                ((ChannelMessageEvent) chanMessageEvent).execute(channel, user, msg);
            }
        }
    }

    public void callConnectEvent() {
        synchronized (registeredEvents) {
            for (BaseEvent connectEvent : registeredEvents.get(EventType.CONNECT)) {
                ((ConnectEvent) connectEvent).execute();
            }
        }
    }

    public void callFileTransferFinishedEvent(DccFileTransfer transfer, Exception ex) {
        synchronized (registeredEvents) {
            for (BaseEvent fileTransferFinishedEvent : registeredEvents.get(EventType.FILE_TRANSFER_FINISHED)) {
                ((FileTransferFinishedEvent) fileTransferFinishedEvent).execute(transfer, ex);
            }
        }
    }

    public void callIncomingChatRequestEvent(DccChat chat) {
        synchronized (registeredEvents) {
            for (BaseEvent incomingChatRequestEvent : registeredEvents.get(EventType.INCOMING_CHAT_REQUEST)) {
                ((IncomingChatRequestEvent) incomingChatRequestEvent).execute(chat);
            }
        }
    }

    public void callJoinEvent(Channel channel, User user) {
        synchronized (registeredEvents) {
            for (BaseEvent joinEvent : registeredEvents.get(EventType.JOIN)) {
                ((JoinEvent) joinEvent).execute(channel, user);
            }
        }
    }

    public void callPartEvent(Channel channel, User user) {
        synchronized (registeredEvents) {
            for (BaseEvent partEvent : registeredEvents.get(EventType.PART)) {
                ((PartEvent) partEvent).execute(channel, user);
            }
        }
    }

    public void callPrivateMessageEvent(Channel channel, User user, String msg) {
        synchronized (registeredEvents) {
            for (BaseEvent privMessageEvent : registeredEvents.get(EventType.PRIVATE_MESSAGE)) {
                ((PrivateMessageEvent) privMessageEvent).execute(channel, user, msg);
            }
        }
    }
}
