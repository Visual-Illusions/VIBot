package net.visualillusionsent.vibot.api.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.visualillusionsent.vibot.io.irc.User;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BotCommand {

    /**
     * Special {@code NULL} value for aliases - DO NOT USE
     */
    static final String NULL = "THIS IS A SPECIAL NULL VALUE - DO NOT USE";

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
