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
package net.visualillusionsent.vibot.api.commands;

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.CommandParser;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Base Command form
 * <p>
 * Commands are set up to auto register them selfs,<br>
 * all that needs to be done is the class to be initialized in the {@link BotPlugin}<br>
 * Example:<br>
 * <code><pre>
 * public void initialize(){
 *     new BaseCommandImpl(this);
 * }
 * </code></pre>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public abstract class BaseCommand {

    /**
     * The {@link BotCommand} annotation for the {@code BaseCommand}
     */
    private final BotCommand cmd;

    /**
     * The {@link BotPlugin} associated with the {@code BaseCommand}
     */
    private final BotPlugin plugin;

    /**
     * Constructs a new {@code BaseCommand} object
     * <p>
     * Requires the {@code BaseCommand} to have the {@link BotCommand} annotation
     * 
     * @param plugin
     *            the BotPlugin creating this command
     */
    public BaseCommand(BotPlugin plugin) {
        if (!getClass().isAnnotationPresent(BotCommand.class)) {
            throw new VIBotException("BotCommand annotation not found!");
        }
        else {
            cmd = getClass().getAnnotation(BotCommand.class);
        }
        this.plugin = plugin;
        CommandParser.getInstance().add(this);
    }

    /**
     * Gets the name of the {@code BaseCommand}
     * 
     * @return the name of the {@code BaseCommand}
     */
    public final String getName() {
        return cmd.main();
    }

    /**
     * Gets all the aliases for the {@code BaseCommand}
     * 
     * @return the aliases for the {@code BaseCommand}
     */
    public final String[] getAliases() {
        return cmd.aliases().clone();
    }

    /**
     * Gets the usage for the {@code BaseCommand}
     * 
     * @return the usage for the {@code BaseCommand}
     */
    public final String getUsage() {
        return cmd.usage();
    }

    /**
     * Gets the description for the {@code BaseCommand}
     * 
     * @return the description for the {@code BaseCommand}
     */
    public final String getDescription() {
        return cmd.desc();
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to have {@code Voice} or above
     * 
     * @return {@code true} if requires {@code Voice}
     */
    public final boolean requiresVoice() {
        return cmd.voice();
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to have {@code Op} or above
     * 
     * @return {@code true} if requires {@code Op}
     */
    public final boolean requiresOp() {
        return cmd.op();
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to be a {@code BotOwner}
     * 
     * @return {@code true} if requires {@code BotOwner}
     */
    public final boolean requiresOwner() {
        return cmd.owner();
    }

    public final boolean isChannelOnly() {
        return cmd.chanOnly();
    }

    public final boolean isConsoleOnly() {
        return cmd.consoleOnly();
    }

    public final BotPlugin getPlugin() {
        return plugin;
    }

    /**
     * Parses the {@code BaseCommand}
     * 
     * @return {@code true} if executed successfully
     */
    public final boolean parseCommand(Channel channel, User user, String[] args) {
        if (args.length < cmd.minParam() || (args.length > cmd.maxParam() && cmd.maxParam() > 0)) {
            onBadSyntax(user, args);
            return false;
        }
        execute(channel, user, args);
        return true;
    }

    /**
     * Sends {@link User} a message about bad syntax
     * 
     * @param user
     *            {@link User} using the command
     * @param args
     *            the command arguments
     */
    public void onBadSyntax(User user, String[] args) {
        user.sendNotice(cmd.usage());
    }

    /**
     * Executes a {@link BaseCommand}. Note: should not be called directly.<br>
     * Use {@link #parseCommand()} instead!
     * 
     * @param channel
     *            the {@link Channel} the command is called from, or null if not called from a {@link Channel}
     * @param user
     *            {@link User} using the command
     * @param args
     *            the command arguments
     */
    abstract public boolean execute(Channel channel, User user, String[] args);

    // Start - Java Object Methods
    /**
     * String representation as BaseCommand[ClassName=%s Aliases=%s Usage=%s ErrorMessage=%s MinParams=%d MaxParams=%d RequireVoice=%b RequireOp=%b RequireBotOwner=%b] format
     * 
     * @return formated string
     */
    @Override
    public final String toString() {
        try {
            return String.format("BaseCommand[ClassName=%s Aliases=%s Usage=%s ErrorMessage=%s MinParams=%d MaxParams=%d RequireVoice=%b RequireOp=%b RequireBotOwner=%b]", this.getClass().getSimpleName(), StringUtils.joinString(cmd.aliases(), ",", 0), cmd.usage(), cmd.desc(), Integer.valueOf(cmd.minParam()), Integer.valueOf(cmd.maxParam()), Boolean.valueOf(cmd.voice()), Boolean.valueOf(cmd.op()), Boolean.valueOf(cmd.owner()));
        }
        catch (UtilityException e) {}
        return null;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof BaseCommand)) {
            return false;
        }
        BaseCommand that = (BaseCommand) other;
        if (cmd != that.cmd) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        //        hash = 53 * hash + (this.usage != null ? this.usage.hashCode() : 0);
        //        hash = 53 * hash + (this.desc != null ? this.desc.hashCode() : 0);
        //        hash = 53 * hash + this.minParam;
        //        hash = 53 * hash + this.maxParam;
        return hash;
    }
    // End - Java Object Methods
}