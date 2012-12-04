package net.visualillusionsent.vibot.io.configuration;

import java.io.File;
import java.io.UnsupportedEncodingException;

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
    private boolean autonickchange = true, ident = true, log_pingpong = false, log_server_pingpong = false;
    private int serv_port = 6667, ident_port = 113;
    private long messageDelay = 750;

    private BotConfig() {
        load();
    }

    public static BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }
        return instance;
    }

    /**
     * Loads the configuration
     */
    private void load() {
        File file = new File("botprops.ini");
        if (!file.exists()) {
            BotLogMan.warning("First Launch detected! Initializing properties and shutting down...");
            props = new PropertiesFile("botprops.ini");
            CheckKeys();
            System.exit(0);
            return;
        }

        props = new PropertiesFile("botprops.ini");
        CheckKeys();
        botname = parseString("VIBot", "Bot-Name");
        login = parseString("VIBot", "Login-Name");
        autonickchange = props.getBoolean("AutoNickChange");
        nickserv_pass = parseString(null, "NickServ-Password");
        server = parseString("my.server.name", "Server");
        server_pass = parseString(null, "Server-Password");
        serv_port = props.getInt("Server-Port");
        ident = props.getBoolean("Use-Ident-Server");
        ident_port = props.getInt("Ident-Port");
        channels = props.getString("Channels").split(",");
        join_message = parseString(null, "Join-Message");
        part_message = parseString(null, "Part-Message");
        quit_message = parseString(null, "Quit-Message");
        messageDelay = props.getInt("Message-Delay");
        cmd_Prefix = props.getCharacter("Command-Prefix");
        bot_owners = parseString("darkdiplomat,darkdiplomat|away", "Bot-Owner-Nicks").split(",");
        plugins = parseString("", "Plugins").split(",");
        checkEncoding();
        BotLogMan.info("Properties Loaded...");
    }

    private void CheckKeys() {
        boolean keymissing = false;
        //botname
        if (!props.containsKey("Bot-Name")) {
            props.setString("Bot-Name", "VIBot", "This is the Nick of the Bot");
            keymissing = true;
        }
        //login
        if (!props.containsKey("Login-Name")) {
            props.setString("Login-Name", "VIBot", "Sets the login of the Bot (not the nick)");
            keymissing = true;
        }
        //autonickchange
        if (!props.containsKey("AutoNickChange")) {
            props.setBoolean("AutoNickChange", true, "Sets if the bot's nick should change if already in use at connect");
            keymissing = true;
        }
        //nickserv_pass
        if (!props.containsKey("NickServ-Password")) {
            props.setString("NickServ-Password", "", "Password to log identify with NickServ if needed");
            keymissing = true;
        }
        //server
        if (!props.containsKey("Server")) {
            props.setString("Server", "my.server.name", "This is the IRC Server to connect to", "Can be an IP address or URL");
            keymissing = true;
        }
        //server_pass
        if (!props.containsKey("Server-Password")) {
            props.setString("Server-Password", "", "Password to log into server if needed");
            keymissing = true;
        }
        //serv_port
        if (!props.containsKey("Server-Port")) {
            props.setInt("Server-Port", 6667, "Port in which to connect to the IRC Server");
            keymissing = true;
        }
        //ident
        if (!props.containsKey("Use-Ident-Server")) {
            props.setBoolean("Use-Ident-Server", true);
            keymissing = true;
        }
        //ident-port
        if (!props.containsKey("Ident-Port")) {
            props.setInt("Ident-Port", 113, "The port to use with the Ident Server");
            keymissing = true;
        }
        //channels
        if (!props.containsKey("Channels")) {
            props.setString("Channels", "", "Channels to join separated by a Comma (,)");
            keymissing = true;
        }
        //join_message
        if (!props.containsKey("Join-Message")) {
            props.setString("Join-Message", "Have no fear, VIBot is here!", "Message to send when joining a channel");
            keymissing = true;
        }
        //part_message
        if (!props.containsKey("Part-Message")) {
            props.setString("Part-Message", "'Till we meet again...", "Message to send when parting a channel");
            keymissing = true;
        }
        //quit_message
        if (!props.containsKey("Quit-Message")) {
            props.setString("Quit-Message", "That's it, I QUIT!", "Message to send when disconnecting from the server");
            keymissing = true;
        }
        //messageDelay
        if (!props.containsKey("Message-Delay")) {
            props.setLong("Message-Delay", 750, "The delay in milliseconds between message sendings (so as to not spam the IRC)");
            keymissing = true;
        }
        //cmd_prefix
        if (!props.containsKey("Command-Prefix")) {
            props.setCharacter("Command-Prefix", '!');
            keymissing = true;
        }
        //bot_owners
        if (!props.containsKey("Bot-Owner-Nicks")) {
            props.setString("Bot-Owner-Nicks", "", "List of admins (used for commands not sent from a channel)");
            keymissing = true;
        }
        //plugins
        if (!props.containsKey("Plugins")) {
            props.setString("Plugins", "", "List of Plugins to load by default. Seperate plugins with a Comma (,)");
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

    private void checkEncoding() {
        String encode = props.getString("Encoding");
        if (encode == null) {
            encoding = "UTF-8";
            return;
        }
        try {
            "".getBytes(encode);
        }
        catch (UnsupportedEncodingException e) {
            BotLogMan.warning("Invaild Encoding... Using UTF-8");
            encoding = "UTF-8";
        }
    }
    
    public static boolean autoNickChange(){
        return getInstance().autonickchange;
    }
    
    public static boolean useIdentServer() {
        return getInstance().ident;
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

    public static long getMessageDelay(){
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
    
    public static String getQuitMessage(){
        return getInstance().quit_message;
    }

    public static String getLogin() {
        return getInstance().login;
    }
    
    public static String getEncoding() {
        return getInstance().encoding;
    }

    public static String[] getPlugins() {
        return getInstance().plugins;
    }

    public static String[] getChannels() {
        return getInstance().channels;
    }

    public static String[] getBotOwners() {
        return getInstance().bot_owners;
    }
}
