/* 
 * Copyright 2012 - 2013 Visual Illusions Entertainment.
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
 * You should have received a copy of the GNU Lesser General Public License along with VIBot.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.api.commands;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.visualillusionsent.vibot.io.irc.Channel;
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
    static final String NULL = "null.NULL#NULL throws null.NilException (WAT)";

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
    int minParam() default 0;

    /**
     * The maximum number of required parameteres
     */
    int maxParam() default Integer.MAX_VALUE;

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
