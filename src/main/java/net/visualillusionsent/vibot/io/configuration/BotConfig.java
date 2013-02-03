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
package net.visualillusionsent.vibot.io.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.IdentServer;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Handles the loading and getting of the {@link VIBot}'s configuration
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class BotConfig {
    private static BotConfig instance;
    private PropertiesFile props;
    private String botname, login, server, server_pass, nickserv_pass, join_message, part_message, quit_message, encoding;
    private String[] plugins = new String[] { "" }, channels = new String[] { "" }, bot_owners = new String[] { "" };
    private char cmd_Prefix = '!';
    private boolean autonickchange = true, ident = true;
    private int serv_port = 6667, ident_port = 113;
    private int[] dcc_ports = new int[] {};
    private long messageDelay = 750;

    /**
     * Constructs a new {@code BotConfig}
     */
    private BotConfig() {
        if (instance != null) {
            throw new IllegalStateException("Only one BotConfig instance may be created at a time.");
        }
        try {
            load();
        }
        catch (UtilityException e) {
            BotLogMan.severe("Properties File Issue: ", e);
            VIBot.terminate("Error Code 2", 2);
            return;
        }
    }

    /**
     * Gets the {@code BotConfig} instance
     * 
     * @return the {@code BotConfig} instance
     */
    public static BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }
        return instance;
    }

    /**
     * Loads the configuration or migrates default props if the properties file doesn't exist
     * 
     * @throws UtilityException
     *             if a property is invalid
     */
    private void load() throws UtilityException {
        File file = new File("botprops.ini");
        if (!file.exists()) {
            BotLogMan.warning("First Launch detected! Initializing properties and exiting. Be sure to set your properties before restarting.");
            migrateProps();
            VIBot.terminate(null, 0);
            return;
        }

        props = new PropertiesFile("botprops.ini");
        botname = props.getString("Bot-Name");
        login = props.getString("Login-Name");
        autonickchange = props.getBoolean("AutoNickChange");
        nickserv_pass = props.getString("NickServ-Password");
        server = props.getString("Server");
        server_pass = props.getString("Server-Password");
        serv_port = props.getInt("Server-Port");
        ident = props.getBoolean("Use-Ident-Server");
        ident_port = props.getInt("Ident-Port");
        channels = props.getString("Channels").split(",");
        join_message = props.getString("Join-Message");
        part_message = props.getString("Part-Message");
        quit_message = props.getString("Quit-Message");
        messageDelay = props.getInt("Message-Delay");
        cmd_Prefix = props.getCharacter("Command-Prefix");
        bot_owners = props.getStringArray("Bot-Owner-Nicks");
        plugins = props.getString("Plugins").split(",");
        dcc_ports = props.getIntArray("dcc-ports");
        checkEncoding();
        BotLogMan.info("Properties Loaded...");
    }

    /**
     * Migrates the default properties from the jar to the base directory for the {@link VIBot}
     * 
     * @throws UtilityException
     */
    private void migrateProps() throws UtilityException {
        InputStream in = null;
        FileWriter out = null;
        try {
            File outputFile = new File("botprops.ini");
            in = getClass().getClassLoader().getResourceAsStream("resources/defaultbotprops.ini");
            out = new FileWriter(outputFile);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        }
        catch (IOException e) {
            BotLogMan.severe("Unable to create properties file!");
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e2) {}
        }
        finally {
            try {
                in.close();
                out.close();
            }
            catch (IOException ioe) {}
        }
    }

    /**
     * Checks for vaild Encoding
     * 
     * @throws UtilityException
     */
    private void checkEncoding() throws UtilityException {
        String encode = props.getString("Encoding");
        try {
            "".getBytes(encode);
            encoding = encode;
        }
        catch (UnsupportedEncodingException e) {
            BotLogMan.warning("Invaild Encoding... Using UTF-8");
            encoding = "UTF-8";
        }
    }

    /**
     * Gets whether the {@link VIBot} nick should autochange for nick in use
     * 
     * @return {@code true} if auto changes; {@code false} otherwise
     */
    public static boolean autoNickChange() {
        return getInstance().autonickchange;
    }

    /**
     * Gets whether to use the {@link IdentServer}
     * 
     * @return {@code true} if to use the {@link IdentServer}; {@code false} otherwise
     */
    public static boolean useIdentServer() {
        return getInstance().ident;
    }

    public static boolean getDebug() {
        return false;
    }

    /**
     * Gets the command prefix character
     * 
     * @return the command prefix character
     */
    public static char getCommandPrefix() {
        return getInstance().cmd_Prefix;
    }

    /**
     * Gets the port to use for the {@link IdentServer}
     * 
     * @return port to use for the {@link IdentServer}
     */
    public static int getIdentPort() {
        return getInstance().ident_port;
    }

    /**
     * Gets the port to use to connect to the IRC Server
     * 
     * @return IRC Server port
     */
    public static int getServerPort() {
        return getInstance().serv_port;
    }

    /**
     * Returns the set of port numbers to be used when sending a DCC chat or
     * file transfer. This is useful when you are behind a firewall and need to
     * set up port forwarding. The array of port numbers is traversed in
     * sequence until a free port is found to listen on. A DCC tranfer will fail
     * if all ports are already in use. If set to null, <i>any</i> free port
     * number will be used.
     * 
     * @return An array of port numbers that VIBot can use to send DCC
     *         transfers, or null if any port is allowed.
     */
    public static int[] getDccPorts() {
        if (getInstance().dcc_ports == null || getInstance().dcc_ports.length == 0) {
            return null;
        }
        // Clone the array to prevent external modification.
        return (int[]) getInstance().dcc_ports.clone();
    }

    /**
     * Gets the delay in miliseconds between messages sent to the IRC Server
     * 
     * @return message delay in miliseconds
     */
    public static long getMessageDelay() {
        return getInstance().messageDelay;
    }

    /**
     * Gets the nick to use for the {@link VIBot}
     * 
     * @return nick for the {@link VIBot}
     */
    public static String getBotName() {
        return getInstance().botname;
    }

    /**
     * Gets the IRC Server URL
     * 
     * @return IRC Server URL
     */
    public static String getServer() {
        return getInstance().server;
    }

    /**
     * Gets the IRC Server password
     * 
     * @return IRC Server Password
     */
    public static String getServerPassword() {
        return getInstance().server_pass;
    }

    /**
     * Gets the NickServ password for the {@link VIBot}
     * 
     * @return NickServ password
     */
    public static String getNickServPassword() {
        return getInstance().nickserv_pass;
    }

    /**
     * Gets the message to send when the {@link VIBot} joins a {@link Channel}
     * 
     * @return join message
     */
    public static String getJoinMessage() {
        return getInstance().join_message;
    }

    /**
     * Gets the message to send when the {@link VIBot} parts a {@link Channel}
     * 
     * @return part message
     */
    public static String getPartMessage() {
        return getInstance().part_message;
    }

    /**
     * Gets the message to send when the {@link VIBot} disconnects from the IRC Server
     * 
     * @return quit message
     */
    public static String getQuitMessage() {
        return getInstance().quit_message;
    }

    /**
     * Gets the login to use for the {@link VIBot}
     * 
     * @return login
     */
    public static String getLogin() {
        return getInstance().login;
    }

    /**
     * Gets the encoding to use with the {@link VIBot}
     * 
     * @return encoding
     */
    public static String getEncoding() {
        return getInstance().encoding;
    }

    /**
     * Gets the list of {@link BotPlugin}s to load by default
     * 
     * @return list of {@link BotPlugin}s
     */
    public static String[] getPlugins() {
        return (String[]) getInstance().plugins.clone();
    }

    /**
     * Gets the list of {@link Channel}s to join by default
     * 
     * @return list of {@link Channel}s
     */
    public static String[] getChannels() {
        return (String[]) getInstance().channels.clone();
    }

    /**
     * Gets the list of {@link User}s who are owners of the {@link VIBot}
     * 
     * @return list of {@link User}s
     */
    public static String[] getBotOwners() {
        return (String[]) getInstance().bot_owners.clone();
    }
}
