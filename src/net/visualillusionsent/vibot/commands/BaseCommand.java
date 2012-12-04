package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class BaseCommand {
    private final String[] aliases;
    private final String usage, desc;
    private final boolean voice, op, owner;
    private final int minParam, maxParam;
    private final BotPlugin plugin;

    /**
     * Class Constructor
     * 
     * @param plugin
     *            the BotPlugin creating this command
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

    public final String getName() {
        return aliases[0];
    }

    public final String[] getAliases() {
        return aliases.clone();
    }

    public final String getUsage() {
        return usage;
    }

    public final String getDescription() {
        return desc;
    }

    public boolean requiresVoice() {
        return voice;
    }

    public boolean requiresOp() {
        return op;
    }

    public boolean requiresOwner() {
        return owner;
    }

    BotPlugin getPlugin() {
        return plugin;
    }

    /**
     * Parses the command
     * 
     * @return true if executed
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
     * Sends caller a message about bad syntax
     * 
     * @param caller
     *            caller of the command
     * @param args
     *            the command arguments
     */
    public void onBadSyntax(User user, String[] args) {
        user.sendNotice(usage);
    }

    /**
     * Executes a command. Note: should not be called directly.<br>
     * Use {@link #parseCommand()} instead!
     * 
     * @param caller
     *            caller of the command
     * @param args
     *            the command arguments
     */
    abstract public boolean execute(Channel channel, User user, String[] args);

    // Start - Java Object Methods
    /**
     * String representation as BaseCommand[name=%s, usage=%s, errormessage=%s, permission=%s, minparam=%d, maxparam=%d] format
     * 
     * @return formated string
     */
    @Override
    public final String toString() {
        return String.format("BaseCommand[name=%s, usage=%s, errormessage=%s, minparam=%d, maxparam=%d]", this.getClass().getSimpleName(), usage, desc, Integer.valueOf(minParam), Integer.valueOf(maxParam));
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
