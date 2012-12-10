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
package net.visualillusionsent.vibot.io;

import java.util.LinkedList;

/**
 * Queue is a definition of a data structure that may
 * act as a queue - that is, data can be added to one end of the
 * queue and data can be requested from the head end of the queue.
 * This class is thread safe for multiple producers and a single
 * consumer. The next() method will block until there is data in
 * the queue.
 * 
 * @since VIBot 1.0
 * @author Jason (darkdiplomat)
 */
public class Queue {
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
     */
    public void add(String msg) {
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
     */
    public void addFront(String msg) {
        synchronized (queue) {
            queue.addFirst(msg);
            queue.notify();
        }
    }

    /**
     * Returns the Message at the front of the Queue. This
     * message is then removed from the Queue. If the Queue
     * is empty, then this method shall block until there
     * is an Object in the Queue to return.
     * 
     * @return The next message from the front of the queue.
     */
    public String next() {

        String msg = null;

        // Block if the Queue is empty.
        synchronized (queue) {
            if (queue.size() == 0) {
                try {
                    queue.wait();
                }
                catch (InterruptedException e) {
                    return null;
                }
            }

            // Return the Object.
            try {
                msg = queue.getFirst();
                queue.removeFirst();
            }
            catch (ArrayIndexOutOfBoundsException e) {
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
     * @return True only if the Queue not empty.
     */
    public boolean hasNext() {
        return (this.size() != 0);
    }

    /**
     * Clears the contents of the Queue.
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
     */
    public int size() {
        return queue.size();
    }
}
