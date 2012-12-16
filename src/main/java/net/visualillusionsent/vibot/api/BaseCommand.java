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
package net.visualillusionsent.vibot.api;

import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.CommandParser;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

public abstract class BaseCommand {

    /**
     * {@link String} array of aliases for the {@code BaseCommand}
     */
    private final String[] aliases;

    /**
     * The usage for the {@code BaseCommand}
     */
    private final String usage;

    /**
     * A description of the {@code BaseCommand}
     */
    private final String desc;

    /**
     * Whether the command is for {@link User}s with {@code Voice} and above
     */
    private final boolean voice;

    /**
     * Whether the command is for {@link User}s with {@code Op} and above
     */
    private final boolean op;

    /**
     * Whether the command is for {@link User}s that are {@code BotOwner}
     */
    private final boolean owner;

    /**
     * The minimum number of required parameters
     */
    private final int minParam;

    /**
     * The maximum number of required parameteres
     */
    private final int maxParam;

    /**
     * The {@link BotPlugin} associated with the {@code BaseCommand}
     */
    private final BotPlugin plugin;

    /**
     * Constructs a new {@code BaseCommand} object
     * 
     * @param plugin
     *            the BotPlugin creating this command
     * @param aliases
     *            the aliases for this command, index 0 should be the name of the command itself
     * @param tooltip
     *            a description of the command
     * @param errorMessage
     *            the error message to show
     * @param permission
     *            the permission require to use the command
     * @param minParam
     *            minimum number of arguments required
     * @param maxParam
     *            maximum number of arguments
     */
    public BaseCommand(BotPlugin plugin, String[] aliases, String usage, String desc, int minParam, int maxParam, boolean requireVoice, boolean requireOp, boolean requireOwner) {
        this.plugin = plugin;
        this.aliases = aliases;
        this.usage = usage;
        this.desc = desc;
        this.minParam = minParam;
        this.maxParam = maxParam;
        this.voice = requireVoice;
        this.op = requireOp;
        this.owner = requireOwner;
        CommandParser.getInstance().add(this);
    }

    /**
     * Gets the name of the {@code BaseCommand}
     * 
     * @return the name of the {@code BaseCommand}
     */
    public final String getName() {
        return aliases[0];
    }

    /**
     * Gets all the aliases for the {@code BaseCommand}
     * 
     * @return the aliases for the {@code BaseCommand}
     */
    public final String[] getAliases() {
        return aliases.clone();
    }

    /**
     * Gets the usage for the {@code BaseCommand}
     * 
     * @return the usage for the {@code BaseCommand}
     */
    public final String getUsage() {
        return usage;
    }

    /**
     * Gets the description for the {@code BaseCommand}
     * 
     * @return the description for the {@code BaseCommand}
     */
    public final String getDescription() {
        return desc;
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to have {@code Voice} or above
     * 
     * @return {@code true} if requires {@code Voice}
     */
    public boolean requiresVoice() {
        return voice;
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to have {@code Op} or above
     * 
     * @return {@code true} if requires {@code Op}
     */
    public boolean requiresOp() {
        return op;
    }

    /**
     * Gets whether the {@code BaseCommand} requires a {@link User} to be a {@code BotOwner}
     * 
     * @return {@code true} if requires {@code BotOwner}
     */
    public boolean requiresOwner() {
        return owner;
    }

    /**
     * Parses the {@code BaseCommand}
     * 
     * @return {@code true} if executed successfully
     */
    public boolean parseCommand(Channel channel, User user, String[] args) {
        if (args.length < minParam || (args.length > maxParam && maxParam > 0)) {
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
        user.sendNotice(usage);
    }

    public BotPlugin getPlugin() {
        return plugin;
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
            return String.format("BaseCommand[ClassName=%s Aliases=%s Usage=%s ErrorMessage=%s MinParams=%d MaxParams=%d RequireVoice=%b RequireOp=%b RequireBotOwner=%b]", this.getClass().getSimpleName(), StringUtils.joinString(aliases, ",", 0), usage, desc, Integer.valueOf(minParam), Integer.valueOf(maxParam), Boolean.valueOf(voice), Boolean.valueOf(op), Boolean.valueOf(owner));
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
        if (!this.usage.equals(that.getUsage())) {
            return false;
        }
        if (!this.desc.equals(that.desc)) {
            return false;
        }
        if (this.minParam != that.minParam) {
            return false;
        }
        return this.maxParam == that.maxParam;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.usage != null ? this.usage.hashCode() : 0);
        hash = 53 * hash + (this.desc != null ? this.desc.hashCode() : 0);
        hash = 53 * hash + this.minParam;
        hash = 53 * hash + this.maxParam;
        return hash;
    }
    // End - Java Object Methods
}
