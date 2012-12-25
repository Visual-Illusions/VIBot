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
package net.visualillusionsent.vibot.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.visualillusionsent.utils.SystemUtils;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Information Command<br>
 * Gives system information about the {@link VIBot}<br>
 * <b>Usage:</b> !info<br>
 * <b>Minimum Params:</b> 1<br>
 * <b>Maximum Params:</b> &infin;<br>
 * <b>Requires:</b> n/a<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "info", usage = "!info", desc = "Gives System Information about this VIBot")
public final class InformationCommand extends BaseCommand {

    /**
     * Constructs a new {@code InformationCommand}
     */
    public InformationCommand() {
        super(null);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        Runtime.getRuntime().gc();
        if (SystemUtils.isWindows()) {
            windowsInfo(channel, user);
        }
        else {
            unixInfo(channel, user);
        }
        return true;
    }

    /**
     * Parses WINDOWS information
     * 
     * @param channel
     *            the {@link Channel} to send info to
     */
    private final void windowsInfo(Channel channel, User user) {
        String cpu = System.getenv("PROCESSOR_IDENTIFIER");
        String bits = System.getenv("PROCESSOR_ARCHITECTURE");
        String cores = System.getenv("NUMBER_OF_PROCESSORS");
        String ramFree = "Free: " + String.format("%.2f %s", ((Runtime.getRuntime().freeMemory() / 1024.0F) / 1024.0F), "Mb");
        String ramTotal = "Total Allocated: " + String.format("%.2f %s", ((Runtime.getRuntime().totalMemory() / 1024.0F) / 1024.0F), "Mb");
        float maxMemory = (float) Runtime.getRuntime().maxMemory();
        String ramMax = "Max Allowed: " + (maxMemory == Float.MAX_VALUE ? "no limit" : String.valueOf((float) maxMemory / 1024.0F / 1024.0F) + "Mb");

        message(channel, user, "OS NAME: " + System.getProperty("os.name"));
        message(channel, user, "OS VERSION: " + System.getProperty("os.version"));
        message(channel, user, "OS ARCH: " + System.getProperty("os.arch"));
        message(channel, user, "CPU: " + cpu);
        message(channel, user, "Architecture: " + (bits != null ? bits : "x86"));
        message(channel, user, "Cores: " + (cores != null ? cores : "1"));
        message(channel, user, "RAM: " + ramFree + " " + ramTotal + " " + ramMax);
    }

    /**
     * Parses UNIX infomation
     * 
     * @param channel
     *            the {@link Channel} to send info to
     */
    private final void unixInfo(Channel channel, User user) {
        channel.sendMessage("OS NAME: " + System.getProperty("os.name"));
        channel.sendMessage("OS VERSION: " + System.getProperty("os.version"));
        channel.sendMessage("OS ARCH: " + System.getProperty("os.arch"));
        try {
            String command = ("cat /proc/cpuinfo");
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                String[] pre = line.split(":");
                if (pre[0].trim().matches("processor|cpu|clock|platform|Memory")) {
                    message(channel, user, pre[0].trim().toUpperCase() + ": " + pre[1].trim());
                }
            }
            in.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private final void message(Channel channel, User user, String message) {
        if (channel != null) {
            channel.sendMessage(message);
        }
        else {
            user.sendNotice(message);
        }
    }
}
