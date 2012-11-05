package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;

public abstract class BaseCommand {

    /**
     * Executes a command. Note: should not be called directly.<br>
     * Use {@link #parseCommand()} instead!
     * 
     * @param caller
     *            - caller of the command
     * @param args
     *            - the command arguments
     */
    public abstract boolean execute(Channel channel, User user, String[] args);

    public final boolean argCheck(int argsRequired, String[] args) {
        return (args.length >= argsRequired);
    }
}
