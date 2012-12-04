package net.visualillusionsent.vibot.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.commands.CommandParser;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Listens for commands/messages given via the Console/Terminal
 * 
 * @since VIBot 1.0
 * @author darkdiplomat
 */
public final class ConsoleCommandReceiver extends Thread {
    private final User user = new User("$", "BOT");
    private final Channel console = new Channel("CONSOLE");
    private final BufferedReader consoleread = new BufferedReader(new InputStreamReader(System.in));

    public ConsoleCommandReceiver() {
        super("ConsoleCommandReceiver-Thread");
        this.setDaemon(true);
    }

    public void run() {
        while (!VIBot.isShuttingDown()) {
            String inLine = null;
            try {
                while ((inLine = consoleread.readLine()) != null) {
                    if (inLine.isEmpty()) {
                        System.out.println("");
                        continue;
                    }

                    String[] args = inLine.split(" ");
                    if (!CommandParser.parseBotCommand(console, user, args)) {
                        String chan = inLine.split(" ")[0];
                        if (chan.startsWith("#")) {
                            if (inLine.length() > (chan.length() + 1)) {
                                VIBot.sendBotMessage(chan, inLine.substring(chan.length() + 1));
                            }
                        }
                    }
                }
            }
            catch (IOException e) {}
            catch (VIBotException vibe) {
                BotLogMan.warning("An exception occured in the ConsoleCommandReceiver-Thread: ", vibe);
            }
        }
        try {
            consoleread.close();
        }
        catch (IOException e) {}
    }
}
