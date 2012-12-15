package net.visualillusionsent.vibot.io.exception;

/**
 * IRCException class
 */
public final class IRCException extends RuntimeException {
    private static final long serialVersionUID = 301909112012L;

    /**
     * Constructs a new IrcException.
     * 
     * @param e
     *            The error message to report.
     */
    public IRCException(String e) {
        super(e);
    }

}
