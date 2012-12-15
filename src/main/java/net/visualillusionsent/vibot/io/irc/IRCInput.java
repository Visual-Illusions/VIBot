package net.visualillusionsent.vibot.io.irc;

import java.io.BufferedReader;
import java.io.InterruptedIOException;

import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A Thread which reads lines from the IRC server. It then passes these lines to
 * the VIBot without changing them. This running Thread also detects
 * disconnection from the server.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public final class IRCInput extends Thread {
    private volatile boolean running = true;
    private final IRCConnection irc_conn;

    /**
     * The InputThread reads lines from the IRC server and allows the VIBot to
     * handle them.
     * 
     * @param bot
     *            An instance of the underlying VIBot.
     * @param breader
     *            The BufferedReader that reads lines from the server.
     * @param bwriter
     *            The BufferedWriter that sends lines to the server.
     */
    public IRCInput(IRCConnection irc_conn) {
        super("IRCInput-Thread");
        this.irc_conn = irc_conn;
    }

    /**
     * Called to start this Thread reading lines from the IRC server. When a
     * line is read, this method calls the handleLine method in the PircBot,
     * which may subsequently call an 'onXxx' method in the PircBot subclass. If
     * any subclass of Throwable (i.e. any Exception or Error) is thrown by your
     * method, then this method will print the stack trace to the standard
     * output. It is probable that the PircBot may still be functioning normally
     * after such a problem, but the existance of any uncaught exceptions in
     * your code is something you should really fix.
     */
    public void run() {
        try {
            while (running) {
                BufferedReader breader = irc_conn.getReader();
                try {
                    String line = null;
                    while ((line = breader.readLine()) != null) {
                        try {
                            irc_conn.handleLine(line);
                        }
                        catch (Throwable t) {
                            BotLogMan.warning("An exception occured in the IRCInput-Thread: ", t);
                        }
                    }
                    if (line == null) {
                        // The server must have disconnected us.
                        running = false;
                    }
                }
                catch (InterruptedIOException iioe) {
                    // This will happen if we haven't received anything from the
                    // server for a while.
                    // So we shall send it a ping to check that we are still
                    // connected.
                    irc_conn.sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        }
        catch (Exception e) {
            // Do nothing.
        }

        // If we reach this point, then we must have disconnected.
        try {
            irc_conn.closeSocket();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }

        irc_conn.disconnected();
    }

    /**
     * Closes the socket without onDisconnect being called subsequently.
     */
    public final synchronized void dispose() {
        running = false;
        interrupt();
        try {
            irc_conn.closeSocket();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }
    }
}
