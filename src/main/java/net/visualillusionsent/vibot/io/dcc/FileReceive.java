/* 
 * Copyright 2012 Visual Illusions Entertainment.
 *  
 * This file is part of VIBot.
 *
 * VIBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIBot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with VIUtils.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 *
 * Parts of this file are derived from PircBot
 * Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/
 *
 * PircBot is dual-licensed, allowing you to choose between the GNU
 * General Public License (GPL) and the www.jibble.org Commercial License.
 * Since the GPL may be too restrictive for use in a proprietary application,
 * a commercial license is also provided. Full license information can be
 * found at http://www.jibble.org/licenses/
 */
package net.visualillusionsent.vibot.io.dcc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

import net.visualillusionsent.utils.IPAddressUtils;
import net.visualillusionsent.vibot.api.plugin.events.EventManager;

/**
 * This class is used to receive a DCC file in a new thread.
 * <p>
 * This class is contains code derived from PircBot <br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, http://www.jibble.org/<br>
 * and dual Licensed under the GNU General Public License/www.jibble.org Commercial License
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 * @version 1.0
 */
class FileReceive extends Thread {
    private DccFileTransfer transfer;
    private boolean resume;

    /**
     * Creates a new FileReceive thread
     * 
     * @param transfer
     *            the DccFileTransfer handler
     * @param resume
     *            whether its being resumed or not
     */
    public FileReceive(DccFileTransfer transfer, boolean resume) {
        super("FileReceive-Thread");
        this.transfer = transfer;
        this.resume = resume;
    }

    /**
     * Runs to receive the file.
     */
    public void run() {

        BufferedOutputStream foutput = null;
        Exception exception = null;

        try {

            // Convert the integer address to a proper IP address.
            String ipStr = IPAddressUtils.ipv4BytestoString(IPAddressUtils.longToIPv4(transfer.getNumericalAddress()));

            // Connect the socket and set a timeout.
            transfer.setSocket(new Socket(ipStr, transfer.getPort()));
            transfer.getSocket().setSoTimeout(30 * 1000);
            transfer.setStartTime(System.currentTimeMillis());

            // No longer possible to resume this transfer once it's
            // underway.
            transfer.getManager().removeAwaitingResume(transfer);

            BufferedInputStream input = new BufferedInputStream(transfer.getSocket().getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(transfer.getSocket().getOutputStream());

            // Following line fixed for jdk 1.1 compatibility.
            foutput = new BufferedOutputStream(new FileOutputStream(transfer.getFile().getCanonicalPath(), resume));

            byte[] inBuffer = new byte[DccFileTransfer.BUFFER_SIZE];
            byte[] outBuffer = new byte[4];
            int bytesRead = 0;
            while ((bytesRead = input.read(inBuffer, 0, inBuffer.length)) != -1) {
                foutput.write(inBuffer, 0, bytesRead);
                transfer.setProgress(transfer.getProgress() + bytesRead);
                // Send back an acknowledgement of how many bytes we
                // have got so far.
                outBuffer[0] = (byte) ((transfer.getProgress() >> 24) & 0xff);
                outBuffer[1] = (byte) ((transfer.getProgress() >> 16) & 0xff);
                outBuffer[2] = (byte) ((transfer.getProgress() >> 8) & 0xff);
                outBuffer[3] = (byte) ((transfer.getProgress() >> 0) & 0xff);
                output.write(outBuffer);
                output.flush();
                delay();
            }
            foutput.flush();
        }
        catch (Exception e) {
            exception = e;
        }
        finally {
            try {
                foutput.close();
                transfer.getSocket().close();
            }
            catch (Exception anye) {
                // Do nothing.
            }
        }

        EventManager.getInstance().callFileTransferFinishedEvent(transfer, exception);
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
