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
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Handles the loading of the bot's configuration
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 * @version 1.0
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

    private BotConfig() {
        try {
            load();
        }
        catch (UtilityException e) {
            BotLogMan.severe("Properties File Issue: ", e);
            VIBot.terminate(2);
            return;
        }
    }

    public static BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }
        return instance;
    }

    /**
     * Loads the configuration
     * 
     * @throws UtilityException
     */
    private void load() throws UtilityException {
        File file = new File("botprops.ini");
        if (!file.exists()) {
            BotLogMan.warning("First Launch detected! Initializing properties and exiting. Be sure to set your properties before restarting.");
            migrateProps();
            System.exit(0);
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

    public static boolean autoNickChange() {
        return getInstance().autonickchange;
    }

    public static boolean useIdentServer() {
        return getInstance().ident;
    }

    public static boolean getDebug() {
        return true;
    }

    public static char getCommandPrefix() {
        return getInstance().cmd_Prefix;
    }

    public static int getIdentPort() {
        return getInstance().ident_port;
    }

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

    public static long getMessageDelay() {
        return getInstance().messageDelay;
    }

    public static String getBotName() {
        return getInstance().botname;
    }

    public static String getServer() {
        return getInstance().server;
    }

    public static String getServerPassword() {
        return getInstance().server_pass;
    }

    public static String getNickServPassword() {
        return getInstance().nickserv_pass;
    }

    public static String getJoinMessage() {
        return getInstance().join_message;
    }

    public static String getPartMessage() {
        return getInstance().part_message;
    }

    public static String getQuitMessage() {
        return getInstance().quit_message;
    }

    public static String getLogin() {
        return getInstance().login;
    }

    public static String getEncoding() {
        return getInstance().encoding;
    }

    public static String[] getPlugins() {
        return (String[]) getInstance().plugins.clone();
    }

    public static String[] getChannels() {
        return (String[]) getInstance().channels.clone();
    }

    public static String[] getBotOwners() {
        return (String[]) getInstance().bot_owners.clone();
    }
}
