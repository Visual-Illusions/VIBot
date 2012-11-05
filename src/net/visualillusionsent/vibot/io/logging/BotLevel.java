package net.visualillusionsent.vibot.io.logging;

import java.util.logging.Level;

public class BotLevel extends Level {

    private static final long serialVersionUID = 180437111987L;

    public static final Level INCOMING = new BotLevel("INCOMING", 2000);
    public static final Level OUTGOING = new BotLevel("OUTGOING", 2100);
    public static final Level PING = new BotLevel("PING", 2200);
    public static final Level SERVERPING = new BotLevel("SERVERPING", 2300);
    public static final Level CHANMESSAGE = new BotLevel("CHANMESSAGE", 2400);
    public static final Level PRIVMESSAGE = new BotLevel("PRIVMESSAGE", 2500);
    public static final Level COMMAND = new BotLevel("COMMAND", 2600);
    public static final Level NOTICE = new BotLevel("NOTICE", 2700);
    public static final Level JOIN = new BotLevel("JOIN", 2800);
    public static final Level PART = new BotLevel("PART", 2900);

    public BotLevel(String arg0, int arg1) {
        super(arg0, arg1);
    }
}
