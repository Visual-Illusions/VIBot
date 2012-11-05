package net.visualillusionsent.vibot.io;

import java.net.*;
import java.util.logging.Logger;
import java.io.*;

import net.visualillusionsent.vibot.VIBot;

/**
 * A simple IdentServer (also know as "The Identification Protocol"). An ident
 * server provides a means to determine the identity of a user of a particular
 * TCP connection.
 * <p>
 * Most IRC servers attempt to contact the ident server on connecting hosts in
 * order to determine the user's identity. A few IRC servers will not allow you
 * to connect unless this information is provided.
 * <p>
 * So when a PircBot is run on a machine that does not run an ident server, it
 * may be necessary to provide a "faked" response by starting up its own ident
 * server and sending out apparently correct responses.
 * <p>
 * An instance of this class can be used to start up an ident server only if it
 * is possible to do so. Reasons for not being able to do so are if there is
 * already an ident server running on port 113, or if you are running as an
 * unprivileged user who is unable to create a server socket on that port
 * number. TODO
 * 
 * @since 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton
 * @version 1.0
 */
public class IdentServer extends Thread {

    private String _login;
    private ServerSocket _ss = null;
    private Logger logger = Logger.getLogger("VIBot");

    /**
     * Constructs and starts an instance of an IdentServer that will respond to
     * a client with the provided login. Rather than calling this constructor
     * explicitly from your code, it is recommended that you use the
     * startIdentServer method in the PircBot class.
     * <p>
     * The ident server will wait for up to 60 seconds before shutting down.
     * Otherwise, it will shut down as soon as it has responded to an ident
     * request.
     * 
     * @param bot
     *            The PircBot instance that will be used to log to.
     * @param login
     *            The login that the ident server will respond with.
     */
    public IdentServer(VIBot bot, String login) {
        _login = login;

        try {
            _ss = new ServerSocket(113);
            _ss.setSoTimeout(120000);
        } catch (Exception e) {
            logger.warning("*** Could not start the ident server on port 113.");
            e.printStackTrace();
            return;
        }

        logger.info("*** Ident server running on port 113 for the next 120 seconds...");
        this.setName(this.getClass() + "-Thread");
        this.start();
    }

    /**
     * Waits for a client to connect to the ident server before making an
     * appropriate response. Note that this method is started by the class
     * constructor.
     */
    public void run() {
        try {
            Socket socket = _ss.accept();
            socket.setSoTimeout(120000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line = reader.readLine();
            while (line != null) {
                logger.info("*** Ident request received: " + line);
                line = line + " : USERID : UNIX : " + _login;
                writer.write(line + "\r\n");
                writer.flush();
                logger.info("*** Ident reply sent: " + line);
                writer.close();
                break;
            }
        } catch (Exception e) {
            // We're not really concerned with what went wrong, are we?
        }

        try {
            _ss.close();
        } catch (Exception e) {
            // Doesn't really matter...
        }

        logger.info("*** The Ident server has been shut down.");
    }
}
