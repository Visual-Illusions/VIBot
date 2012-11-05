package net.visualillusionsent.vibot.plugin.hook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

public class HookManager {
    private static HookManager instance;
    private HashMap<String, JoinHook> join = new HashMap<String, JoinHook>();
    private HashMap<String, MessageHook> message = new HashMap<String, MessageHook>();

    public static HookManager getInstance() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null) {
            instance = new HookManager();
        }
    }

    public static void addHook(BaseHook hook) {
        getInstance().add(hook);
    }

    public static void removeHook(BaseHook hook) {
        getInstance().remove(hook);
    }

    private void add(BaseHook hook) {
        if (hook instanceof JoinHook) {
            if (!join.containsKey(hook.getClass().getName())) {
                join.put(hook.getClass().getName(), (JoinHook) hook);
            }
        } else if (hook instanceof MessageHook) {
            if (!message.containsKey(hook.getClass().getName())) {
                message.put(hook.getClass().getName(), (MessageHook) hook);
            }
        }
    }

    private void remove(BaseHook hook) {
        if (hook instanceof JoinHook) {
            join.remove(hook.getClass().getName());
        } else if (hook instanceof MessageHook) {
            message.remove(hook.getClass().getName());
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
    
    public List<BaseHook> getAllHooks(){
        List<BaseHook> phaseone = new ArrayList<BaseHook>(join.values());
        List<BaseHook> phasetwo = new ArrayList<BaseHook>(message.values());
        BaseHook[] phase1 = join.values().toArray(new BaseHook[]{});
        BaseHook[] phase2 = message.values().toArray(new BaseHook[]{});
        Collections.copy(phaseone, Arrays.asList(phase1));
        Collections.copy(phasetwo, Arrays.asList(phase2));
        phaseone.addAll(phasetwo);
        return phaseone;
    }
}
