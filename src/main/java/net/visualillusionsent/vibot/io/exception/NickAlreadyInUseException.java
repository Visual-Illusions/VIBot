package net.visualillusionsent.vibot.io.exception;

/**
 * NickAlreadyInUseException
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 * @code.derivative PircBot
 */
public final class NickAlreadyInUseException extends RuntimeException {
    private static final long serialVersionUID = 301910112012L;

    /**
     * Constructs a new {@code NickAlreadyInUseException}
     * 
     * @param msg
     *            The error message to report.
     */
    public NickAlreadyInUseException(String msg) {
        super(msg);
    }

}
