package net.visualillusionsent.vibot.plugin.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public class HookManager {
    private static HookManager instance;
    private HashMap<String, JoinHook> join = new HashMap<String, JoinHook>();
    private HashMap<String, MessageHook> message = new HashMap<String, MessageHook>();

    public static HookManager getInstance() {
        if (instance == null) {
            instance = new HookManager();
        }
        return instance;
    }

    public static void addHook(BaseHook hook) {
        getInstance().add(hook);
    }

    public static void removeHook(BaseHook hook) {
        getInstance().remove(hook);
    }

    private void add(BaseHook hook) {
        switch (hook.getType()) {
            case JOIN:
                if (!join.containsKey(hook.getClass().getName())) {
                    join.put(hook.getClass().getName(), (JoinHook) hook);
                }
                break;
            case MESSAGE:
                if (!message.containsKey(hook.getClass().getName())) {
                    message.put(hook.getClass().getName(), (MessageHook) hook);
                }
                break;
            default:
                break;
        }
    }

    private void remove(BaseHook hook) {
        switch (hook.getType()) {
            case JOIN:
                join.remove(hook.getClass().getName());
                break;
            case MESSAGE:
                message.remove(hook.getClass().getName());
                break;
            default:
                break;
        }
    }

    public void callJoinHook(Channel channel, User user) {
        if (!join.isEmpty()) {
            synchronized (join) {
                for (JoinHook jh : join.values()) {
                    jh.execute(channel, user);
                }
            }
        }
    }

    public void callMessageHook(Channel channel, User user, String msg) {
        if (!message.isEmpty()) {
            synchronized (message) {
                for (MessageHook mh : message.values()) {
                    mh.execute(channel, user, msg);
                }
            }
        }
    }

    public void removePluginHooks(BotPlugin plugin) {
        List<BaseHook> tempList = new ArrayList<BaseHook>();
        for (BaseHook hook : join.values()) {
            if (hook.getPlugin().equals(plugin)) {
                tempList.add(hook);
            }
        }
        for (BaseHook hook : message.values()) {
            if (hook.getPlugin().equals(plugin)) {
                tempList.add(hook);
            }
        }

        for (BaseHook hook : tempList) {
            removeHook(hook);
        }
    }
}
