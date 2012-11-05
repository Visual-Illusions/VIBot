package net.visualillusionsent.vibot.plugin.hook;

import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class BaseHook {
    private BotPlugin plugin;

    public BaseHook(BotPlugin plugin) {
        this.plugin = plugin;
    }

    public BotPlugin getPlugin() {
        return plugin;
    }
}
