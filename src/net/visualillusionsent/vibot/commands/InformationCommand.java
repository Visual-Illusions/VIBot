package net.visualillusionsent.vibot.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

@BotCommand(aliases = { "info" }, usage = "!info", desc = "Gives System Information about this Bot")
final class InformationCommand extends BaseCommand {

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            unixInfo(channel);
        } else {
            String cpu = System.getenv("PROCESSOR_IDENTIFIER");
            String bits = System.getenv("PROCESSOR_ARCHITECTURE");
            String cores = System.getenv("NUMBER_OF_PROCESSORS");
            String ramFree = "Free: " + String.valueOf((float) Runtime.getRuntime().freeMemory() / 1024.0F / 1024.0F) + "Mb";
            float maxMemory = (float) Runtime.getRuntime().maxMemory();
            String ramMax = "Max: " + (maxMemory == Float.MAX_VALUE ? "no limit" : String.valueOf((float) maxMemory / 1024F / 1024F) + "Mb");
            String ramTotal = "Total: " + String.valueOf((float) Runtime.getRuntime().totalMemory() / 1024F / 1024F) + "Mb";

            channel.sendMessage("CPU: " + (cpu != null ? cpu : "PowerPC 750cx (600 MHz)"));
            channel.sendMessage("Architecture: " + (bits != null ? bits : "x86"));
            channel.sendMessage("Cores: " + (cores != null ? cores : "1"));
            channel.sendMessage("RAM: " + ramFree + " " + ramMax + " " + ramTotal);
        }
        return true;
    }

    private final void unixInfo(Channel channel) {
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
                    channel.sendMessage(pre[0].trim().toUpperCase() + ": " + pre[1].trim());
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
