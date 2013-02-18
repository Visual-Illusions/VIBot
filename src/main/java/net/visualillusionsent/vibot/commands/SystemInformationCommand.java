/* 
 * Copyright 2012 - 2013 Visual Illusions Entertainment.
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
 * You should have received a copy of the GNU Lesser General Public License along with VIBot.
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
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
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
@BotCommand(main = "sysinfo", usage = "!sysinfo", desc = "Gives System Information about this VIBot")
public final class SystemInformationCommand extends BaseCommand {

    /**
     * Constructs a new {@code InformationCommand}
     */
    public SystemInformationCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public final boolean execute(Channel channel, User user, String[] args) {
        Runtime.getRuntime().gc();
        if (SystemUtils.isWindows()) {
            windowsInfo(channel, user);
        }
        else if (SystemUtils.isUnix()) {
            unixInfo(channel, user);
        }
        else {
            message(channel, user, "Unable to determin System Information...");
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

        message(channel, user, "OS Name: " + System.getProperty("os.name"));
        message(channel, user, "OS Version: " + System.getProperty("os.version"));
        message(channel, user, "OS Architecture: " + System.getProperty("os.arch"));
        message(channel, user, "CPU Model: " + cpu);
        message(channel, user, "Architecture: " + (bits != null ? bits : "unknown"));
        message(channel, user, "Cores: " + (cores != null ? cores : "unknown"));
        message(channel, user, "RAM: " + ramFree + " " + ramTotal + " " + ramMax);
    }

    /**
     * Parses UNIX infomation
     * 
     * @param channel
     *            the {@link Channel} to send info to
     */
    private final void unixInfo(Channel channel, User user) {
        int cores = 0;
        String vID = "";
        String cpuModel = "";
        try {
            String command = ("cat /proc/cpuinfo");
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                String[] pre = line.split(":");
                if (pre[0].trim().startsWith("processor")) {
                    cores++;
                }
                else if (pre[0].trim().startsWith("vendor_id") && vID.isEmpty()) {
                    vID = pre[1].trim();
                }
                else if (pre[0].trim().startsWith("model name") && cpuModel.isEmpty()) {
                    cpuModel = pre[1].trim();
                }
            }
            in.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        String ramFree = "Free: " + String.format("%.2f Mb", ((Runtime.getRuntime().freeMemory() / 1024.0F) / 1024.0F));
        String ramTotal = "Total Allocated: " + String.format("%.2f Mb", ((Runtime.getRuntime().totalMemory() / 1024.0F) / 1024.0F));
        float maxMemory = (float) Runtime.getRuntime().maxMemory();
        String ramMax = "Max Allowed: " + (maxMemory == Float.MAX_VALUE ? "no limit" : String.format("%.2f Mb", (float) maxMemory / 1024.0F / 1024.0F));

        message(channel, user, "OS Name: " + System.getProperty("os.name"));
        message(channel, user, "OS Version: " + System.getProperty("os.version"));
        message(channel, user, "OS Architecture: " + System.getProperty("os.arch"));
        message(channel, user, "CPU Cores: " + cores);
        message(channel, user, "Vendor ID: " + (vID.isEmpty() ? "unknown" : vID));
        message(channel, user, "CPU Model: " + (cpuModel.isEmpty() ? "unknown" : cpuModel));
        message(channel, user, "JVM RAM Allocations: " + ramFree + " " + ramTotal + " " + ramMax);
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
