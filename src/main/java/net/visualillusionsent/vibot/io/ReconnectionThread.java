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

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

public final class ReconnectionThread extends Thread {
    private VIBot bot;

    public ReconnectionThread(VIBot bot) {
        super("ReconnectionThread-Thread");
        this.bot = bot;
    }

    public void run() {
        while (!bot.isConnected()) {
            try {
                BotLogMan.info("Attempting reconnection...");
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
                BotLogMan.warning("Reconnect failed... Trying again in 2 minutes...");
            }
            try {
                sleep(1200000);
            }
            catch (InterruptedException e) {}
        }
    }
}