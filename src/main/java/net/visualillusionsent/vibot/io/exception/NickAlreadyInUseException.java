package net.visualillusionsent.vibot.io.exception;

/**
 * NickAlreadyInUseException
 */
public final class NickAlreadyInUseException extends RuntimeException {
    private static final long serialVersionUID = 301910112012L;

    public NickAlreadyInUseException(String e) {
        super(e);
    }

}
