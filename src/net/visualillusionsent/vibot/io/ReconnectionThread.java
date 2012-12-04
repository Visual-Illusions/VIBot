package net.visualillusionsent.vibot.io;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

public final class ReconnectionThread extends Thread {
    private VIBot bot;
    
    public ReconnectionThread(VIBot bot){
        super("ReconnectionThread-Thread");
        this.bot = bot;
    }

    public void run() {
        while (!bot.isConnected()) {
            try {
                System.out.println("Attempting reconnection...");
                if (BotConfig.useIdentServer()) {
                    try {
                        new IdentServer(bot);
                    }
                    catch (VIBotException vibe) {
                        BotLogMan.warning("", vibe);
                    }
                }
                bot.reconnect();
            }
            catch (Exception e) {
                System.out.println("Reconnect failed... Trying again in 1 minute...");
            }
            try {
                sleep(60000);
            }
            catch (InterruptedException e) {}
        }
    }
}
