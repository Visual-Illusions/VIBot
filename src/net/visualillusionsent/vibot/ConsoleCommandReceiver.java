package net.visualillusionsent.vibot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.visualillusionsent.vibot.commands.CommandParser;

/**
 * Listens for commands/messages given via the Console/Terminal
 * 
 * @since VIBot 1.0
 * @author darkdiplomat
 * @version 1.0
 */
final class ConsoleCommandReceiver extends Thread {
    private User user = new User("$", "BOTCONSOLE");
    private BufferedReader consoleread = new BufferedReader(new InputStreamReader(System.in));

    ConsoleCommandReceiver() {
        this.setName(this.getClass() + "-Thread");
    }

    public void run() {
        while (!VIBotMain.isShuttingDown()) {
            String inLine = null;
            try {
                while ((inLine = consoleread.readLine()) != null) {
                    if (inLine.equals("")) continue;

                    String[] args = inLine.split(" ");
                    if (!CommandParser.parseBotCommand(new Channel("CONSOLE"), user, args)) {
                        String chan = inLine.split(" ")[0];
                        if (chan.startsWith("#")) {
                            if (inLine.length() > (chan.length() + 1)) {
                                VIBotMain.bot.sendMessage(chan, inLine.substring(chan.length() + 1));
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}
