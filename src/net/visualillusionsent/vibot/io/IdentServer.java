package net.visualillusionsent.vibot.io;

import java.net.*;
import java.io.*;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * A simple IdentServer (also know as "The Identification Protocol"). An ident
 * server provides a means to determine the identity of a user of a particular
 * TCP connection.
 * <p>
 * Most IRC servers attempt to contact the ident server on connecting hosts in
 * order to determine the user's identity. A few IRC servers will not allow you
 * to connect unless this information is provided.
 * <p>
 * So when a VIBot is run on a machine that does not run an ident server, it may
 * be necessary to provide a "faked" response by starting up its own ident
 * server and sending out apparently correct responses.
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 */
public class IdentServer extends Thread {
    private ServerSocket ss = null;

    /**
     * Constructs and starts an instance of an IdentServer that will respond to
     * a client with the provided login.
     * <p>
     * The ident server will wait for up to 120 seconds before shutting down.
     * Otherwise, it will shut down as soon as it has responded to an ident
     * request.
     * 
     * @param bot
     *            The VIBot instance that will be used to log to.
     * @throws VIBotException
     */
    public IdentServer(VIBot bot) throws VIBotException {
        super("IdentServer-Thread");
        int port = BotConfig.getIdentPort();
        try {
            ss = new ServerSocket(port);
            ss.setSoTimeout(120000);
        }
        catch (Exception e) {
            throw new VIBotException("Could not start the ident server on port ".concat(String.valueOf(port)).concat("."), e);
        }

        BotLogMan.info("Ident server running on port ".concat(String.valueOf(BotConfig.getIdentPort())).concat(" for the next 120 seconds..."));
        this.start();
    }

    /**
     * Waits for a client to connect to the ident server before making an
     * appropriate response. Note that this method is started by the class
     * constructor.
     */
    public void run() {
        try {
            Socket socket = ss.accept();
            socket.setSoTimeout(120000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line = reader.readLine();
            while (line != null) {
                BotLogMan.info("Ident request received: " + line);
                line = line.concat(" : USERID : UNIX : ").concat(BotConfig.getLogin());
                writer.write(line.concat("\r\n"));
                writer.flush();
                BotLogMan.info("Ident reply sent: ".concat(line));
                writer.close();
                break;
            }
        }
        catch (Exception e) {
            // We're not really concerned with what went wrong, are we?
        }

        try {
            ss.close();
        }
        catch (Exception e) {
            // Doesn't really matter...
        }

        BotLogMan.info("The Ident server has been shut down.");
    }
}
