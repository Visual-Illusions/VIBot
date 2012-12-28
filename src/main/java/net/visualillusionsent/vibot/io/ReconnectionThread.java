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
package net.visualillusionsent.vibot.io;

import java.io.IOException;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Reconnection Thread<br>
 * Handles reconnecting to the server if the {@link VIBot} gets disconnected without being told to do so.
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class ReconnectionThread extends Thread {
    private VIBot bot;

    public ReconnectionThread(VIBot bot) {
        super("ReconnectionThread-Thread");
        this.bot = bot;
        this.setDaemon(true);
    }

    public void run() {
        while (!VIBot.isConnected() && !VIBot.isShuttingDown()) {
            try {
                BotLogMan.info("Attempting reconnection...");
                if (BotConfig.useIdentServer()) {
                    try {
                        new IdentServer(bot);
                    }
                    catch (IOException ioe) {
                        BotLogMan.warning("", ioe);
                    }
                }
                bot.reconnect();
            }
            catch (Exception e) {
                BotLogMan.warning("Reconnect failed... Trying again in 2 minutes...");
                e.printStackTrace(); //Sometimes more info is required, but storing to the log isnt nessary
            }
            try {
                sleep(120000);
            }
            catch (InterruptedException e) {}
        }
    }
}
