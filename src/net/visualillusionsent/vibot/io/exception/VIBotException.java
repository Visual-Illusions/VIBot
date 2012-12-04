package net.visualillusionsent.vibot.io.exception;

public class VIBotException extends Exception {
    private static final long serialVersionUID = 021436122012L;

    public VIBotException(String msg) {
        super(msg);
    }

    public VIBotException(String msg, Throwable thrown) {
        super(msg, thrown);
    }

}
