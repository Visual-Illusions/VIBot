package net.visualillusionsent.vibot;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;

/**
 * Fake Plugin<br>
 * FOR INTERNAL USE ONLY
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
final class FakePlugin extends BotPlugin {

    public FakePlugin(VIBot bot) {
        super(bot);
    }

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public void disable() {}

    @Override
    public void initialize() {}
}
