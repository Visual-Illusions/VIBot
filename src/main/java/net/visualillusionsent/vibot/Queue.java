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
package net.visualillusionsent.vibot;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Queue is a definition of a data structure that may
 * act as a queue - that is, data can be added to one end of the
 * queue and data can be requested from the head end of the queue.
 * This class is thread safe for multiple producers and a single
 * consumer. The next() method will block until there is data in
 * the queue.
 * <p>
 * This class contains code derived from PircBot<br>
 * PircBot is Copyrighted: Paul James Mutton, 2001-2009, <a href="http://www.jibble.org/">http://www.jibble.org/</a><br>
 * and dual Licensed under the <a href="http://www.gnu.org/licenses/gpl.html">GNU General Public License</a>/<a href="http://www.jibble.org/licenses/commercial-license.php">www.jibble.org Commercial License</a>
 * 
 * @since 1.0
 * @version 1.1
 * @author Jason (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 */
public class Queue {
    /**
     * The {@link LinkedList} of queued messages
     */
    private LinkedList<String> queue = new LinkedList<String>();

    /**
     * Constructs a Queue object of unlimited size.
     */
    public Queue() {}

    /**
     * Adds a message to the end of the Queue.
     * 
     * @param msg
     *            The Message to be added to the Queue.
     * @code.derivative PircBot
     */
    public final void add(String msg) {
        synchronized (queue) {
            queue.add(msg);
            queue.notify();
        }
    }

    /**
     * Adds a Message to the front of the Queue.
     * 
     * @param msg
     *            The Message to be added to the Queue.
     * @code.derivative PircBot
     */
    public final void addFront(String msg) {
        synchronized (queue) {
            queue.addFirst(msg);
            queue.notify();
        }
    }

    /**
     * Returns the Message at the front of the Queue.<br>
     * This message is then removed from the Queue.<br>
     * If the Queue is empty,<br>
     * then this method shall block until there is an Object in the Queue to return.
     * 
     * @return The next message from the front of the queue.
     * @code.derivative PircBot
     */
    public final String next() {

        String msg = null;

        // Wait if the Queue is empty.
        synchronized (queue) {
            if (queue.size() == 0) {
                try {
                    queue.wait();
                }
                catch (InterruptedException e) {
                    // Interrupted
                    return null;
                }
            }

            // Return the Object.
            try {
                msg = queue.getFirst();
                queue.removeFirst();
            }
            catch (NoSuchElementException nsee) {
                throw new InternalError("Race hazard in LinkedList object.");
            }
        }

        return msg;
    }

    /**
     * Returns true if the Queue is not empty. If another
     * Thread empties the Queue before <b>next()</b> is
     * called, then the call to <b>next()</b> shall block
     * until the Queue has been populated again.
     * 
     * @return {@code true} only if the Queue not empty.
     * @code.derivative PircBot
     */
    public final boolean hasNext() {
        return (this.size() != 0);
    }

    /**
     * Clears the contents of the Queue.
     * 
     * @code.derivative PircBot
     */
    public void clear() {
        synchronized (queue) {
            queue.clear();
        }
    }

    /**
     * Returns the size of the Queue.
     * 
     * @return The current size of the queue.
     * @code.derivative PircBot
     */
    public int size() {
        return queue.size();
    }
}
