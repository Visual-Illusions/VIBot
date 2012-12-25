package net.visualillusionsent.vibot.io.exception;

/**
 * IRCException class
 */
public final class IRCException extends RuntimeException {
    private static final long serialVersionUID = 301909112012L;

    /**
     * Constructs a new IrcException.
     * 
     * @param msg
     *            The error message to report.
     */
    public IRCException(String msg) {
        super(msg);
    }

}
