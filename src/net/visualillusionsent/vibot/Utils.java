package net.visualillusionsent.vibot;

/**
 * A Singleton class for miscellaneous utility methods and fields
 * 
 * @author Jason (darkdiplomat)
 */
public class Utils {

    /**
     * The System Line Separator (Windows = \r\n Unix = \n Older Macs = \r)
     */
    public static String LINE_SEP = System.getProperty("line.separator");

    /**
     * Combines a {@link String} array into a single String
     * 
     * @param startindex
     *            the index to start combining
     * @param args
     *            the {@link String} array to be combined
     * @param spacer
     *            the {@link String} to put between each element of the array
     * @return the newly combined {@link String}
     */
    public static String combineSplit(int startindex, String[] args, String spacer) {
        StringBuilder sb = new StringBuilder();
        for (int i = startindex; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(spacer);
        }
        return sb.toString();
    }
}
