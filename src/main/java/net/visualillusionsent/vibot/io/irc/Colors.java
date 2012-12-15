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
 */
package net.visualillusionsent.vibot.io.irc;

/**
 * This class contains constants that are useful for formatting lines sent to
 * IRC servers. These constants allow you to apply various formatting to the
 * lines, such as colours, boldness, underlining and reverse text or removing
 * colors and formatting from lines.
 * 
 * @since VIBot 1.0
 * @author Jason Jones (darkdiplomat)
 */
public final class Colors {

    /**
     * Color character.
     */
    public static final String COLOR_CHAR = "\u0003";

    /**
     * Removes all previously applied color and formatting attributes.
     */
    public static final String NORMAL = "\u000f";

    /**
     * <b>BOLD</b> text.
     */
    public static final String BOLD = "\u0002";

    /**
     * <u>UNDERLINED</u> text.
     */
    public static final String UNDERLINE = "\u001f";

    /**
     * <i>REVERSED</i> text (may be rendered as italic text in some clients).
     */
    public static final String REVERSE = "\u0016";

    /**
     * WHITE colored text.
     */
    public static final String WHITE = COLOR_CHAR.concat("00");

    /**
     * <font color=000000>BLACK</font> colored text.
     */
    public static final String BLACK = COLOR_CHAR.concat("01");

    /**
     * <font color=000080>DARK BLUE</font> colored text.
     */
    public static final String DARK_BLUE = COLOR_CHAR.concat("02");

    /**
     * <font color=008000>DARK GREEN</font> coloured text.
     */
    public static final String DARK_GREEN = COLOR_CHAR.concat("03");

    /**
     * <font color=FF0000>RED</font> coloured text.
     */
    public static final String RED = COLOR_CHAR.concat("04");

    /**
     * <font color=804040>BROWN</font> coloured text.
     */
    public static final String BROWN = COLOR_CHAR.concat("05");

    /**
     * <font color=8000FF>PURPLE</font> coloured text.
     */
    public static final String PURPLE = COLOR_CHAR.concat("06");

    /**
     * <font color=808000>OLIVE</font> coloured text.
     */
    public static final String OLIVE = COLOR_CHAR.concat("07");

    /**
     * <font color=FFFF00>YELLOW</font> coloured text.
     */
    public static final String YELLOW = COLOR_CHAR.concat("08");

    /**
     * <font color=00FF00>GREEN</font> coloured text.
     */
    public static final String GREEN = COLOR_CHAR.concat("09");

    /**
     * <font color=008080>TEAL</font> coloured text.
     */
    public static final String TEAL = COLOR_CHAR.concat("10");

    /**
     * <font color=00FFFF>CYAN</font> coloured text.
     */
    public static final String CYAN = COLOR_CHAR.concat("11");

    /**
     * <font color=0000FF>BLUE</font> coloured text.
     */
    public static final String BLUE = COLOR_CHAR.concat("12");

    /**
     * <font color=FF00FF>MAGENTA</font> coloured text.
     */
    public static final String MAGENTA = COLOR_CHAR.concat("13");

    /**
     * <font color=808080>DARK GRAY</font> coloured text.
     */
    public static final String DARK_GRAY = COLOR_CHAR.concat("14");

    /**
     * <font color=C0C0C0>LIGHT GRAY</font> coloured text.
     */
    public static final String LIGHT_GRAY = COLOR_CHAR.concat("15");

    /**
     * This class should not be constructed.
     */
    private Colors() {}

    /**
     * Removes all colors from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but with all colors removed.
     */
    public static String removeColors(String line) {
        String temp = line.replaceAll(COLOR_CHAR + "[0-9]{2}", "");
        return temp;
    }

    /**
     * Remove formatting from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but without any bold, underlining, reverse, etc.
     */
    public static String removeFormatting(String line) {
        String temp = line.replace("[" + NORMAL + "|" + BOLD + "|" + UNDERLINE + "|" + REVERSE + "]", "");
        return temp;
    }

    /**
     * Removes all formatting and colors from a line of IRC text.
     * 
     * @param line
     *            the input text.
     * @return the same text, but without formatting and color characters.
     */
    public static String removeFormattingAndColors(String line) {
        return removeFormatting(removeColors(line));
    }
}
