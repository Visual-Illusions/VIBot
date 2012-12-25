package net.visualillusionsent.vibot.api.commands;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.visualillusionsent.vibot.io.irc.User;

/**
 * {@code @interface} for {@link BaseCommand}<br>
 * Used to specify the command name, aliases, usage, description, min/max paramerters, who can use the command, and where the command can be used
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BotCommand {

    /**
     * Special {@code null} value for aliases - DO NOT USE
     */
    static final String NULL = "null.NULL#NULL throws null.NilException";

    /**
     * Main name for the {@link BaseCommand}
     */
    String main();

    /**
     * {@link String} array of aliases for the {@link BaseCommand}
     */
    String[] aliases() default NULL;

    /**
     * The usage for the {@link BaseCommand}
     */
    String usage();

    /**
     * A description of the {@link BaseCommand}
     */
    String desc();

    /**
     * The minimum number of required parameters
     */
    int minParam() default 1;

    /**
     * The maximum number of required parameteres
     */
    int maxParam() default -1;

    /**
     * Whether the {@link BaseCommand} is for {@link User}s with {@code Voice} and above
     */
    boolean voice() default false;

    /**
     * Whether the {@link BaseCommand} is for {@link User}s with {@code Op} and above
     */
    boolean op() default false;

    /**
     * Whether the {@link BaseCommand} is for {@link User}s that are {@code BotOwner}
     */
    boolean owner() default false;

    /**
     * Whether the {@link BaseCommand} is a {@link Channel} only command
     */
    boolean chanOnly() default false;

    /**
     * Whether the {@link BaseCommand} is a {@code Console} only command
     */
    boolean consoleOnly() default false;

}
