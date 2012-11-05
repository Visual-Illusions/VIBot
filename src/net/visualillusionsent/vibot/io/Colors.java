package net.visualillusionsent.vibot.io;

/**
 * The Colors class provides several static fields and methods that you may find
 * useful when writing an IRC Bot.
 * <p>
 * This class contains constants that are useful for formatting lines sent to
 * IRC servers. These constants allow you to apply various formatting to the
 * lines, such as colours, boldness, underlining and reverse text.
 * <p>
 * The class contains static methods to remove colours and formatting from lines
 * of IRC text.
 * <p>
 * This class is based on/contains code from PircBot PircBot is Copyrighted:
 * Paul James Mutton, 2001-2009, http://www.jibble.org/
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 * @author Paul James Mutton (PircBot)
 * @version 1.0
 */
public class Colors {

    private static final String[] colors = new String[] { "\u000300", "\u000301", "\u000302", "\u000303", "\u000304", "\u000305", "\u000306", "\u000307", "\u000308", "\u000309", "\u000310", "\u000311", "\u000312", "\u000313", "\u000314", "\u000315" };

    private static final String[] formatting = new String[] { "\u000f", "\u0002", "\u001f", "\u001f", "\u0016" };

    /**
     * Removes all previously applied color and formatting attributes.
     */
    public static final String NORMAL = formatting[0];

    /**
     * Bold text.
     */
    public static final String BOLD = "\u0002";

    /**
     * Underlined text.
     */
    public static final String UNDERLINE = "\u001f";

    /**
     * Reversed text (may be rendered as italic text in some clients).
     */
    public static final String REVERSE = "\u0016";

    /**
     * Color character.
     */
    public static final String COLOR_CHAR = "\u0003";

    /**
     * White colored text.
     */
    public static final String WHITE = colors[0];

    /**
     * Black colored text.
     */
    public static final String BLACK = colors[1];

    /**
     * Dark blue colored text.
     */
    public static final String DARK_BLUE = colors[2];

    /**
     * Dark green coloured text.
     */
    public static final String DARK_GREEN = colors[3];

    /**
     * Red coloured text.
     */
    public static final String RED = colors[4];

    /**
     * Brown coloured text.
     */
    public static final String BROWN = colors[5];

    /**
     * Purple coloured text.
     */
    public static final String PURPLE = colors[6];

    /**
     * Olive coloured text.
     */
    public static final String OLIVE = colors[7];

    /**
     * Yellow coloured text.
     */
    public static final String YELLOW = colors[8];

    /**
     * Green coloured text.
     */
    public static final String GREEN = colors[9];

    /**
     * Teal coloured text.
     */
    public static final String TEAL = colors[10];

    /**
     * Cyan coloured text.
     */
    public static final String CYAN = colors[11];

    /**
     * Blue coloured text.
     */
    public static final String BLUE = colors[12];

    /**
     * Magenta coloured text.
     */
    public static final String MAGENTA = colors[13];

    /**
     * Dark gray coloured text.
     */
    public static final String DARK_GRAY = colors[14];

    /**
     * Light gray coloured text.
     */
    public static final String LIGHT_GRAY = colors[15];

    /**
     * This class should not be constructed.
     */
    private Colors() {
    }

    /**
     * Removes all colors from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but with all colors removed.
     * @since VIBot 1.0
     */
    public static String removeColors(String line) {
        for (String color : colors) {
            line.replace(color, "");
        }
        return line;
    }

    /**
     * Remove formatting from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but without any bold, underlining, reverse, etc.
     * @since VIBot 1.0
     */
    public static String removeFormatting(String line) {
        for (String form : formatting) {
            line.replace(form, "");
        }
        return line;
    }

    /**
     * Removes all formatting and colors from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but without formatting and color characters.
     * @since VIBot 1.0
     */
    public static String removeFormattingAndColors(String line) {
        return removeFormatting(removeColors(line));
    }
}
