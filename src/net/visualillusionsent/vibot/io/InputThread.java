package net.visualillusionsent.vibot.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InterruptedIOException;
import java.net.Socket;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A Thread which reads lines from the IRC server. It then passes these lines to
 * the PircBot without changing them. This running Thread also detects
 * disconnection from the server and is thus used by the OutputThread to send
 * lines to the server.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public class InputThread extends Thread {

    private VIBot bot = null;
    private Socket socket = null;
    private BufferedReader breader = null;
    private BufferedWriter bwriter = null;
    private boolean isConnected = true;
    private boolean disposed = false;

    public static final int MAX_LINE_LENGTH = 512;

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
    public InputThread(VIBot bot, Socket socket, BufferedReader breader, BufferedWriter bwriter) {
        super("InputThread-Thread");
        this.bot = bot;
        this.socket = socket;
        this.breader = breader;
        this.bwriter = bwriter;
    }

    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     * 
     * @param line
     *            The raw line to send to the IRC server.
     */
    public void sendRawLine(String line) {
        OutputThread.sendRawLine(bot, bwriter, line);
    }

    /**
     * Returns true if this InputThread is connected to an IRC server. The
     * result of this method should only act as a rough guide, as the result may
     * not be valid by the time you act upon it.
     * 
     * @return True if still connected.
     */
    public boolean isConnected() {
        return isConnected;
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
            boolean running = true;
            while (running) {
                try {
                    String line = null;
                    while ((line = breader.readLine()) != null) {
                        try {
                            bot.handleLine(line);
                        }
                        catch (Throwable t) {
                            BotLogMan.warning("An exception occured in the InputThread: ", t);
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
                    this.sendRawLine("PING " + (System.currentTimeMillis() / 1000));
                    // Now we go back to listening for stuff from the server...
                }
            }
        }
        catch (Exception e) {
            // Do nothing.
        }

        // If we reach this point, then we must have disconnected.
        try {
            socket.close();
        }
        catch (Exception e) {
            // Just assume the socket was already closed.
        }

        if (!disposed) {
            BotLogMan.warning("Disconnected from server.");
            isConnected = false;
            new ReconnectionThread(bot).start();
        }

    }

    /**
     * Closes the socket without onDisconnect being called subsequently.
     */
    public void dispose() {
        try {
            disposed = true;
            socket.close();
        }
        catch (Exception e) {
            // Do nothing.
        }
    }
}
