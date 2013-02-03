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
package net.visualillusionsent.vibot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.commands.DisablePluginCommand;
import net.visualillusionsent.vibot.commands.DisconnectCommand;
import net.visualillusionsent.vibot.commands.EchoCommand;
import net.visualillusionsent.vibot.commands.EnablePluginCommand;
import net.visualillusionsent.vibot.commands.HelpCommand;
import net.visualillusionsent.vibot.commands.IdentifyCommand;
import net.visualillusionsent.vibot.commands.IgnoreUserCommand;
import net.visualillusionsent.vibot.commands.InformationCommand;
import net.visualillusionsent.vibot.commands.InviteCommand;
import net.visualillusionsent.vibot.commands.JoinChannelCommand;
import net.visualillusionsent.vibot.commands.ListIgnoresCommand;
import net.visualillusionsent.vibot.commands.ListPluginsCommand;
import net.visualillusionsent.vibot.commands.NickChangeCommand;
import net.visualillusionsent.vibot.commands.OkThanksCommand;
import net.visualillusionsent.vibot.commands.PartChannelCommand;
import net.visualillusionsent.vibot.commands.PingCommand;
import net.visualillusionsent.vibot.commands.ReloadPluginCommand;
import net.visualillusionsent.vibot.commands.ShutTheFuckUpCommand;
import net.visualillusionsent.vibot.commands.TopicCommand;
import net.visualillusionsent.vibot.commands.UnignoreUserCommand;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Command parsing class
 * <p>
 * Handle parsing of {@link BaseCommand}s supplied to the {@link VIBot}<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 * @see net.visualillusionsent.vibot.commands
 */
public final class CommandParser {
    /**
     * CommandParser instance
     */
    private static CommandParser instance;

    /**
     * Synchronization lock object
     */
    private static final Object lock = new Object();

    /**
     * HashMap of command names to their {@link BaseCommand} counterpart
     */
    private final HashMap<String, BaseCommand> commands;

    /**
     * Constructs a new {@code CommandParser}<br>
     * Should not be constructed externally
     */
    private CommandParser() {
        if (instance != null) {
            throw new IllegalStateException("Only one CommandParser instance may be created at a time.");
        }
        commands = new HashMap<String, BaseCommand>();
    }

    /**
     * Gets the {@code CommandParser} instance<br>
     * If the instance is null, the method will create a new instance and initialize the internal {@link BaseCommand}s
     * 
     * @return {@code CommandParser} instance
     * @see net.visualillusionsent.vibot.commands
     */
    public static final CommandParser getInstance() {
        if (instance == null) {
            instance = new CommandParser();
            new DisablePluginCommand(VIBot.FAKE_PLUGIN);
            new DisconnectCommand(VIBot.FAKE_PLUGIN);
            new EchoCommand(VIBot.FAKE_PLUGIN);
            new EnablePluginCommand(VIBot.FAKE_PLUGIN);
            new HelpCommand(VIBot.FAKE_PLUGIN);
            new IdentifyCommand(VIBot.FAKE_PLUGIN);
            new IgnoreUserCommand(VIBot.FAKE_PLUGIN);
            new InformationCommand(VIBot.FAKE_PLUGIN);
            new InviteCommand(VIBot.FAKE_PLUGIN);
            new JoinChannelCommand(VIBot.FAKE_PLUGIN);
            new ListIgnoresCommand(VIBot.FAKE_PLUGIN);
            new ListPluginsCommand(VIBot.FAKE_PLUGIN);
            new NickChangeCommand(VIBot.FAKE_PLUGIN);
            new OkThanksCommand(VIBot.FAKE_PLUGIN);
            new PartChannelCommand(VIBot.FAKE_PLUGIN);
            new PingCommand(VIBot.FAKE_PLUGIN);
            new ReloadPluginCommand(VIBot.FAKE_PLUGIN);
            new ShutTheFuckUpCommand(VIBot.FAKE_PLUGIN);
            new TopicCommand(VIBot.FAKE_PLUGIN);
            new UnignoreUserCommand(VIBot.FAKE_PLUGIN);
        }
        return instance;
    }

    /**
     * Adds a {@link BaseCommand} to the server list.
     * 
     * @param cmd
     *            the {@link BaseCommand} to add
     */
    public final void add(BaseCommand cmd) {
        if (cmd != null) {
            if (!commands.containsKey(cmd.getName())) {
                commands.put(cmd.getName(), cmd);
            }
            else {
                BotLogMan.warning("Command: '".concat(cmd.getName()).concat("' is already registered!"));
                return;
            }
            if (!cmd.getAliases()[0].equals(BotCommand.NULL)) {
                for (String alias : cmd.getAliases()) {
                    if (!commands.containsValue(alias)) {
                        commands.put(alias, cmd);
                    }
                    else {
                        BotLogMan.warning("Command: '".concat(alias).concat("' is already registered!"));
                    }
                }
            }
        }
    }

    /**
     * Performs a lookup for a command of the given name and executes it if
     * found. Returns false if command not found.
     * 
     * @param channel
     *            the {@link Channel} the {@link BaseCommand} is used from
     * @param user
     *            the {@link User} using the the {@link BaseCommand}
     * @param args
     *            the arguments for the {@link BaseCommand}
     * @return {@code true} if is parsed successfully
     * @throws VIBotException
     *             if an exception occurrs while parsing the command
     */
    public static final boolean parseBotCommand(Channel channel, User user, String[] args) throws VIBotException {
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
                    if (cmd.isChannelOnly() && channel == null) {
                        user.sendNotice("Command can only be used from a channel!");
                        return false;
                    }
                    if (cmd.isConsoleOnly() && (channel != Channel.CONSOLE)) {
                        user.sendNotice("Command can only be used from the Console!");
                        return false;
                    }

                    return cmd.parseCommand(channel, user, args);
                }
                catch (Exception e) {
                    throw new VIBotException("Exception occured while parsing Command: ".concat(args[0]), e);
                }
            }
            user.sendNotice("Unknown Command. Use !help to see availible commands.");
        }
        return false;
    }

    /**
     * Gets the {@link BaseCommand} by name from the commands map
     * 
     * @param command
     *            the name of the {@link BaseCommand} to get
     * @return the {@link BaseCommand} if found; {@code null} otherwise
     */
    private final BaseCommand getCommand(String command) {
        return commands.get(command);
    }

    /**
     * Prints out the help list to the {@link User} based on their status in the channel
     * 
     * @param channel
     *            the {@link Channel} help is being called from
     * @param user
     *            the {@link User} calling for help
     */
    public static final void printHelp(Channel channel, User user) {
        synchronized (lock) {
            user.sendNotice("-- Help List for you in Channel: ".concat(channel.getName()).concat(" --"));
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
                if (cmd.isConsoleOnly() && (channel != Channel.CONSOLE)) {
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

    /**
     * Removes all {@link BaseCommand}s associated with the {@link BotPlugin}
     * 
     * @param plugin
     *            the {@link BotPlugin} to remove {@link BaseCommand}s for
     */
    public final void removePluginCommands(BotPlugin plugin) {
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
