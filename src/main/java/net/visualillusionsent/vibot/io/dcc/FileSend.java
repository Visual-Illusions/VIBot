package net.visualillusionsent.vibot.io.dcc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.vibot.api.events.EventManager;
import net.visualillusionsent.vibot.io.configuration.BotConfig;

/**
 * This class is used to send a DCC file in a new thread.
 * <p>
 * This class is contains code derived from PircBot <br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, http://www.jibble.org/<br>
 * and dual Licensed under the GNU General Public License/www.jibble.org Commercial License
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
class FileSend extends Thread {
    private DccFileTransfer transfer;
    private int timeout;
    private boolean allowResume;

    public FileSend(DccFileTransfer transfer, int timeout, boolean allowResume) {
        this.transfer = transfer;
        this.timeout = timeout;
        this.allowResume = allowResume;
    }

    /**
     * Runs to send the file.
     * 
     * @code.derivative PircBot
     */
    public void run() {

        BufferedInputStream finput = null;
        Exception exception = null;

        try {

            ServerSocket ss = null;

            int[] ports = BotConfig.getDccPorts();
            if (ports == null) {
                // Use any free port.
                ss = new ServerSocket(0);
            }
            else {
                for (int i = 0; i < ports.length; i++) {
                    try {
                        ss = new ServerSocket(ports[i]);
                        // Found a port number we could use.
                        break;
                    }
                    catch (Exception e) {
                        // Do nothing; go round and try another port.
                    }
                }
                if (ss == null) {
                    // No ports could be used.
                    throw new IOException("All ports returned by getDccPorts() are in use.");
                }
            }

            ss.setSoTimeout(timeout);
            transfer.setPort(ss.getLocalPort());
            InetAddress inetAddress = transfer.getIRCConnection().getDccInetAddress();
            if (inetAddress == null) {
                inetAddress = transfer.getIRCConnection().getInetAddress();
            }
            byte[] ip = inetAddress.getAddress();
            long ipNum = IPAddressUtils.ipv4ToLong(ip);

            // Rename the filename so it has no whitespace in it when we
            // send it.
            String safeFilename = transfer.getFile().getName().replace(' ', '_').replace('\t', '_');

            if (allowResume) {
                transfer.getManager().addAwaitingResume(transfer);
            }

            // Send the message to the user, telling them where to
            // connect to in order to get the file.
            transfer.getIRCConnection().sendCTCPCommand(transfer.getUser().getNick(), "DCC SEND ".concat(safeFilename).concat(" ").concat(String.valueOf(ipNum)).concat(" ").concat(String.valueOf(transfer.getPort())).concat(" ").concat(String.valueOf(transfer.getFile().length())));

            // The client may now connect to us and download the file.
            transfer.setSocket(ss.accept());
            transfer.getSocket().setSoTimeout(30000);
            transfer.setStartTime(System.currentTimeMillis());

            // No longer possible to resume this transfer once it's
            // underway.
            if (allowResume) {
                transfer.getManager().removeAwaitingResume(transfer);
            }

            // Might as well close the server socket now; it's finished
            // with.
            ss.close();

            BufferedOutputStream output = new BufferedOutputStream(transfer.getSocket().getOutputStream());
            BufferedInputStream input = new BufferedInputStream(transfer.getSocket().getInputStream());
            finput = new BufferedInputStream(new FileInputStream(transfer.getFile()));

            // Check for resuming.
            if (transfer.getProgress() > 0) {
                long bytesSkipped = 0;
                while (bytesSkipped < transfer.getProgress()) {
                    bytesSkipped += finput.skip(transfer.getProgress() - bytesSkipped);
                }
            }

            byte[] outBuffer = new byte[DccFileTransfer.BUFFER_SIZE];
            byte[] inBuffer = new byte[4];
            int bytesRead = 0;
            while ((bytesRead = finput.read(outBuffer, 0, outBuffer.length)) != -1) {
                output.write(outBuffer, 0, bytesRead);
                output.flush();
                input.read(inBuffer, 0, inBuffer.length);
                transfer.setProgress(transfer.getProgress() + bytesRead);
                delay();
            }
        }
        catch (Exception e) {
            exception = e;
        }
        finally {
            try {
                finput.close();
                transfer.getSocket().close();
            }
            catch (Exception e) {
                // Do nothing.
            }
        }

        EventManager.activateFileTransferFinishedEvent(transfer, exception);
    }

    /**
     * Delay between packets.
     */
    private void delay() {
        if (transfer.getPacketDelay() > 0) {
            try {
                Thread.sleep(transfer.getPacketDelay());
            }
            catch (InterruptedException e) {
                // Do nothing.
            }
        }
    }
}
