package net.visualillusionsent.vibot.plugin.hook;

import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class BaseHook {
    private final BotPlugin plugin;
    private final HookType type;

    public BaseHook(BotPlugin plugin, HookType type) {
        this.plugin = plugin;
        this.type = type;
        HookManager.addHook(this);
    }

    public BotPlugin getPlugin() {
        return plugin;
    }
    
    public HookType getType(){
        return type;
    }
}