package net.visualillusionsent.vibot.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {

    /**
     * A list of aliases for the command. The first alias is the most important
     * -- it is the main name of the command. (The method name is never used for
     * anything).
     * 
     * @return Aliases for a command
     */
    String[] aliases();

    /**
     * Usage instruction. Example text for usage could be
     * <code>[-h harps] [name] [message]</code>.
     * 
     * @return Usage instructions for a command
     */
    String usage();

    /**
     * @return A short description for the command.
     */
    String desc();

    boolean oponly() default false;

    boolean adminonly() default false;

}
