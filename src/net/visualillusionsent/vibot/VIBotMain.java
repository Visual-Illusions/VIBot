package net.visualillusionsent.vibot;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.visualillusionsent.vibot.io.IrcException;
import net.visualillusionsent.vibot.io.NickAlreadyInUseException;

public class VIBotMain {
    private static ConsoleCommandReceiver ccr = new ConsoleCommandReceiver();
    private static boolean shuttingdown = false;

    static Misc misc;
    static boolean isLoaded = false;
    static Config conf;
    static VIBot bot;

    public static Logger logger = Logger.getLogger("VIBot");

    public static void main(String[] args) {
        conf = new Config();

        logger.info("*** VIBot v2.0.0 Started");
        logger.info("*** Loading Properties...");
        conf.load();
        bot = new VIBot();
        misc = new Misc();
        bot.setName(conf.botname);
        bot.setLogin(conf.identd);
        bot.setAutoNickChange(true);
        if (conf.useidentd) {
            bot.startIdentServer();
        }
        try {
            bot.connect(conf.host, conf.port, conf.password);
        } catch (NickAlreadyInUseException e) {
            logger.severe("*** An Nick was already in use... Bot will now terminate...");
            System.exit(1);
        } catch (IOException e) {
            logger.severe("*** An IOException has occured... Bot will now terminate...");
            System.exit(2);
        } catch (IrcException e) {
            logger.severe("*** An IrcException has occured... Bot will now terminate...");
            System.exit(3);
        }
        ccr.setDaemon(true);
        ccr.start();
    }

    final static boolean isShuttingDown() {
        return shuttingdown;
    }

    public final static void terminate() {
        shuttingdown = true;
        if (bot.isConnected()) {
            bot.quitServer("'Till we meet again...");
        }
        bot.dispose();
        Handler[] hand = logger.getHandlers();
        for (Handler han : hand) {
            han.close();
        }
        System.exit(0);
    }
}
