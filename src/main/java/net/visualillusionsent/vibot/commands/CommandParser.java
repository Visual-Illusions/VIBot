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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public class CommandParser {
    private static CommandParser instance;
    private static final Object lock = new Object();
    private final LinkedHashMap<String, BaseCommand> commands = new LinkedHashMap<String, BaseCommand>();

    private CommandParser() {}

    /**
     * Add a command to the server list.
     * 
     * @param name
     * @param cmd
     */
    public void add(BaseCommand cmd) {
        if (cmd != null) {
            for (String alias : cmd.getAliases()) {
                if (!commands.containsValue(alias)) {
                    commands.put(alias, cmd);
                }
                else {
                    BotLogMan.warning("Command: '" + alias + "' is already registered!");
                }
            }
        }
    }

    public static CommandParser getInstance() {
        if (instance == null) {
            instance = new CommandParser();
            new DisablePluginCommand();
            new DisconnectCommand();
            new EchoCommand();
            new EnablePluginCommand();
            new HelpCommand();
            new IgnoreUserCommand();
            new InformationCommand();
            new JoinChannelCommand();
            new IdentifyCommand();
            new ListIgnoresCommand();
            new ListPluginsCommand();
            new NickChangeCommand();
            new OkThanksCommand();
            new PartChannelCommand();
            new PingCommand();
            new ReloadPluginCommand();
            new ShutTheFuckUpCommand();
            new TopicCommand();
            new UnignoreUserCommand();
        }
        return instance;
    }

    /**
     * Performs a lookup for a command of the given name and executes it if
     * found. Returns false if command not found.
     * 
     * @param command
     * @param caller
     * @param args
     * @return
     * @throws VIBotException
     */
    public static boolean parseBotCommand(Channel channel, User user, String[] args) throws VIBotException {
        synchronized (lock) {
            BaseCommand cmd = getInstance().getCommand(args[0]);
            if (cmd != null) {
                try {
                    if (cmd.requiresVoice() && !(user.hasVoice() || user.isOp() || user.isBotOwner() || user.isConsole())) {
                        user.sendNotice("You do not have permission to use that command!");
                        return false;
                    }
                    if (cmd.requiresOp() && !(user.isBotOwner() || user.isOp() || user.isConsole())) {
                        user.sendNotice("You do not have permission to use that command!");
                        return false;
                    }
                    if (cmd.requiresOwner() && !(user.isBotOwner() || user.isConsole())) {
                        user.sendNotice("You do not have permission to use that command!");
                        return false;
                    }
                    return cmd.execute(channel, user, args);
                }
                catch (Exception e) {
                    throw new VIBotException("Exception occured while parsing Command: ".concat(args[0]), e);
                }
            }
            user.sendNotice("Unknown Command. Use !help to see availible commands.");
        }
        return false;
    }

    public BaseCommand getCommand(String command) {
        return commands.get(command);
    }

    public static final void printHelp(Channel channel, User user) {
        synchronized (lock) {
            user.sendNotice("-- Help List for you in Channel: " + channel.getName() + " --");
            List<BaseCommand> triggered = new ArrayList<BaseCommand>();
            for (BaseCommand cmd : instance.commands.values()) {
                if (triggered.contains(cmd)) {
                    continue;
                }
                else if (cmd.getAliases().length > 1) {
                    triggered.add(cmd);
                }

                if (cmd.requiresVoice() && !(user.hasVoice() || user.isOp() || user.isBotOwner() || user.isConsole())) {
                    continue;
                }
                if (cmd.requiresOp() && !(user.isBotOwner() || user.isOp() || user.isConsole())) {
                    continue;
                }
                if (cmd.requiresOwner() && !(user.isBotOwner() || user.isConsole())) {
                    continue;
                }

                user.sendNotice(cmd.getUsage().concat(" - ").concat(cmd.getDescription()));
                if (cmd.getAliases().length > 1) {
                    StringBuilder builder = new StringBuilder();
                    for (String alias : cmd.getAliases()) {
                        builder.append(alias);
                        builder.append(" ");
                    }
                    user.sendNotice("Aliases for ".concat(cmd.getName()).concat(": ").concat(builder.toString()));
                }
            }
        }
    }

    public void removePluginCommands(BotPlugin plugin) {
        synchronized (lock) {
            List<String> toRemove = new ArrayList<String>();
            for (String cmdName : commands.keySet()) {
                BaseCommand cmd = commands.get(cmdName);
                if (cmd.getPlugin() != null && cmd.getPlugin().equals(plugin)) {
                    toRemove.add(cmdName);
                }
            }
            for (String toRem : toRemove) {
                commands.remove(toRem);
            }
        }
    }
}
