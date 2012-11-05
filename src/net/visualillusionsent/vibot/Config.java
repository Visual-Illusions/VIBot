package net.visualillusionsent.vibot;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import net.visualillusionsent.vibot.io.logging.LogFormat;

/**
 * Handles the loading of the bot's configuration
 * 
 * @since VIBot 1.0
 * @author darkdiplomat
 * @version 1.0
 */
class Config {
    private Logger logger = Logger.getLogger("VIBot");
    protected PropertiesFile props;
    protected String botname = "VIBot", host = "irc.esper.net", password = null, joinmess = null, identd = "VIBot";
    protected String[] plugins = new String[] { "" }, channels = new String[] { "" }, admins = new String[] { "" };
    protected char cmdPrefix = '!';
    protected boolean autonickchange = true, useidentd = true, log_pingpong = false, log_server_pingpong = false;
    protected int port = 6667;

    Config() {
        setLogger();
    }

    /**
     * Loads the configuration
     * 
     * @since VIBot 1.0
     */
    void load() {
        boolean fld = false;
        File file = new File("botprops.ini");
        if (!file.exists()) {
            fld = true;
            logger.info("*** First Launch detected! Initializing properties and shutting down...");
            props = new PropertiesFile("botprops.ini");
            CheckKeys();
            System.exit(0);
        }

        if (!fld) {
            props = new PropertiesFile("botprops.ini");
            CheckKeys();

            plugins = parseString("", "Plugins").split(",");
            botname = parseString("VIBot", "Bot-Name");
            host = parseString("", "Host");
            password = parseString(null, "NickServ-Password");
            useidentd = props.getBoolean("Use-IdentdServer");
            identd = props.getString("Identd");
            channels = props.getString("Channels").split(",");
            port = props.getInt("Port");
            autonickchange = props.getBoolean("AutoNickChange");
            admins = parseString("darkdiplomat,darkdiplomat|away", "Admins").split(",");
            joinmess = parseString(null, "JoinMessage");
        }

        logger.info("*** Properties Loaded...");
    }

    private void CheckKeys() {
        boolean keymissing = false;
        if (!props.containsKey("Plugins")) {
            props.setString("Plugins", "", new String[] { "List of Plugins to load by default. Seperate plugins with a Comma (,)" });
            keymissing = true;
        }
        if (!props.containsKey("Bot-Name")) {
            props.setString("Bot-Name", "VIBot", new String[] { "This is the Nick of the Bot" });
            keymissing = true;
        }
        if (!props.containsKey("Host")) {
            props.setString("Host", "", new String[] { "This is the IRC Server to connect to", ";Can be an IP address or URL" });
            keymissing = true;
        }
        if (!props.containsKey("Port")) {
            props.setInt("Port", 6667, new String[] { "Port in which to connect to the IRC Server" });
            keymissing = true;
        }
        if (!props.containsKey("Channels")) {
            props.setString("Channels", "", new String[] { "Channels to join separated by a Comma (,)" });
            keymissing = true;
        }
        if (!props.containsKey("Identd")) {
            props.setString("Identd", "VIBot", new String[] { "Sets the identity of your bot" });
            keymissing = true;
        }
        if (!props.containsKey("AutoNickChange")) {
            props.setBoolean("AutoNickChange", true);
            keymissing = true;
        }
        if (!props.containsKey("Use-IdentdServer")) {
            props.setBoolean("Use-IdentdServer", true);
            keymissing = true;
        }
        if (!props.containsKey("Command-Prefix")) {
            props.setCharacter("Command-Prefix", '!');
            keymissing = true;
        }
        if (!props.containsKey("Admins")) {
            props.setString("Admins", "", new String[] { "List of admins (used for commands not sent from a channel)" });
            keymissing = true;
        }
        if (!props.containsKey("JoinMessage")) {
            props.setString("JoinMessage", "Have no fear, VIBot is here!", new String[] { "Message to send when joining a channel" });
            keymissing = true;
        }
        if (keymissing) {
            props.save();
        }
    }

    private String parseString(String def, String key) {
        String toRet = props.getString(key);

        if (toRet == null) {
            toRet = def;
        }

        return toRet;
    }

    private void setLogger() {
        File LogDir = new File("Log/");
        if (!LogDir.exists()) {
            LogDir.mkdirs();
        }
        try {
            LogFormat lf = new LogFormat();
            ConsoleHandler chand = new ConsoleHandler();
            FileHandler fhand = new FileHandler("Log/botlog%g.log", 52428800, 150, true);

            logger.setUseParentHandlers(false);
            chand.setFormatter(lf);
            fhand.setFormatter(lf);
            fhand.setEncoding("UTF-8");
            logger.addHandler(chand);
            logger.addHandler(fhand);
        } catch (IOException e) {
            logger.warning("Fail to initialize Logging Formats!");
        }
    }
}
