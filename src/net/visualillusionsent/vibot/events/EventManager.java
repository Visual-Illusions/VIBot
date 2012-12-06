package net.visualillusionsent.vibot.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
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
