package net.visualillusionsent.vibot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.plugin.BotPluginLoader;

public class Misc {

    private VIBot bot;
    private static BotPluginLoader loader;
    private HashMap<String, ArrayList<String>> ignorelist = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> mutedin = new ArrayList<String>();
    private HashMap<String, String> plugcommands = new HashMap<String, String>();
    public static Misc instance;
    public static String LINE_SEP = System.getProperty("line.separator");

    Misc() {
        instance = this;
        this.bot = VIBotMain.bot;
        loader = new BotPluginLoader();
        loader.loadPlugins();
    }

    public static Misc getInstance() {
        return instance;
    }

    public static BotPluginLoader getLoader() {
        return loader;
    }

    public static String combineSplit(int startindex, String[] args, String splitchar) {
        StringBuilder sb = new StringBuilder();
        for (int i = startindex; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(splitchar);
        }
        return sb.toString();
    }

    public static void sendConsoleMessage(String message) {
        System.out.println(message);
    }

    static void removeIgnoredChannel(String channel) {
        if (instance.ignorelist.containsKey(channel)) {
            instance.ignorelist.remove(channel);
        }
    }

    public static void addIgnored(String channel, String nick) {
        if (instance.ignorelist.containsKey(channel)) {
            if (!instance.ignorelist.get(channel).contains(nick)) {
                instance.ignorelist.get(channel).add(nick);
            }
        } else {
            instance.ignorelist.put(channel, new ArrayList<String>());
            instance.ignorelist.get(channel).add(nick);
        }
    }

    public static void removeIgnored(String channel, String nick) {
        if (instance.ignorelist.containsKey(channel)) {
            if (instance.ignorelist.get(channel).contains(nick)) {
                instance.ignorelist.get(channel).remove(nick);
            }
        }
    }

    public static boolean isIgnored(String channel, String nick) {
        if (instance.ignorelist.containsKey(channel)) {
            if (instance.ignorelist.get(channel) != null && instance.ignorelist.get(channel).contains(nick)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getIgnoreList(String channel) {
        if (instance.ignorelist.containsKey(channel)) {
            return instance.ignorelist.get(channel);
        }
        return new ArrayList<String>();
    }

    static boolean isMuted(String channel) {
        return instance.mutedin.contains(channel);
    }

    public static void Mute(String channel) {
        if (!instance.mutedin.contains(channel)) {
            instance.mutedin.add(channel);
        }
    }

    public static void unMute(String channel) {
        if (instance.mutedin.contains(channel)) {
            instance.mutedin.remove(channel);
        }
    }

    public static String[] getPlugins() {
        return VIBotMain.conf.plugins;
    }

    public static void addCommand(String command, String help) {
        instance.plugcommands.put(command, help);
    }

    public static void removeCommand(String command) {
        instance.plugcommands.remove(command);
    }

    public static void sendBotMessage(String target, String message) {
        instance.bot.sendMessage(target, message);
    }

    public static void sendBotAction(String target, String action) {
        instance.bot.sendAction(target, action);
    }

    public static String getBotNick() {
        return instance.bot.getNick();
    }

    static boolean isAdmin(String nick) {
        for (String admin : VIBotMain.conf.admins) {
            if (admin.equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public static void joinChannel(String channel) {
        if (!channel.startsWith("#")) {
            return;
        }
        VIBotMain.bot.joinChannel(channel);
    }

    public static void partChannel(String channel, String reason) {
        if (!channel.startsWith("#")) {
            return;
        }
        if (reason != null && !reason.trim().isEmpty()) {
            VIBotMain.bot.partChannel(channel, reason);
        } else {
            VIBotMain.bot.partChannel(channel, "disconnect.genericReason");
        }
    }

    public static void changeNick(String nick) {
        if (nick == null || nick.trim().isEmpty()) {
            return;
        }
        VIBotMain.bot.changeNick(nick);
    }

    public static void identify() {
        VIBotMain.bot.identify(VIBotMain.conf.password);
    }
    
    public List<Channel> getAllChannels(){
        return Collections.unmodifiableList(Arrays.asList(VIBotMain.bot.getChannels()));
    }
}
