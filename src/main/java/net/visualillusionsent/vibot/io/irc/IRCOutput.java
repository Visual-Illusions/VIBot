package net.visualillusionsent.vibot.io.irc;

import net.visualillusionsent.vibot.io.configuration.BotConfig;

/**
 * A Thread which is responsible for sending messages to the IRC server.
 * Messages are obtained from the outgoing message queue and sent immediately if
 * possible. If there is a flood of messages, then to avoid getting kicked from
 * a channel, we put a small delay between each one.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public class IRCOutput extends Thread {
    private final IRCConnection irc_conn;

    /**
     * Constructs an OutputThread for the underlying VIBot. All messages sent
     * to the IRC server are sent by this OutputThread to avoid hammering the
     * server. Messages are sent immediately if possible. If there are multiple
     * messages queued, then there is a delay imposed.
     * 
     * @param bot
     *            The underlying VIBot instance.
     * @param outQueue
     *            The Queue from which we will obtain our messages.
     */
    public IRCOutput(IRCConnection irc_conn) {
        super("IRCOutput-Thread");
        this.irc_conn = irc_conn;
    }

    /**
     * This method starts the Thread consuming from the outgoing message Queue
     * and sending lines to the server.
     */
    public void run() {
        try {
            while (true) {
                // Small delay to prevent spamming of the channel
                sleep(BotConfig.getMessageDelay());

                String line = irc_conn.getQueueNext();
                if (line != null) {
                    irc_conn.sendRawLine(line);
                }
                else {
                    break;
                }
            }
        }
        catch (InterruptedException e) {
            // Just let the method return naturally...
        }
    }

    public final synchronized void dispose() {
        interrupt();
    }
}
