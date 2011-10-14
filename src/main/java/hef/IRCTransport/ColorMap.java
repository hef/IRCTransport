package hef.IRCTransport;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.jibble.pircbot.Colors;

/** Minecraft <-> IRC Color Mapping class
 * Converts Color codes from IRC to Minecraft and from Minecraft to IRC.
 */
public final class ColorMap {
    /** Regex for matching irc colors. */
    private static Pattern ircColorPattern = Pattern.compile('\u0003' + "[0-9]{1,2}(?:,[0-9]{1,2})?");
    /** Map IRC Colors to IRC Colors. */
    private static HashMap<String, String> ircToMinecraftColor = new HashMap<String, String>();
    /** Maps minecraft colors to irc colors. */
    private static HashMap<String, String> minecraftToIrcColor = new HashMap<String, String>();
    /** Regex for matching Minecraft colors. */
    private static Pattern minecraftColorPattern = Pattern.compile('\u00A7' + "[0-9a-z]");
    static {
        ircToMinecraftColor.put(Colors.WHITE, ChatColor.WHITE.toString()); // black
        ircToMinecraftColor.put(Colors.BLACK, ChatColor.BLACK.toString()); // white
        ircToMinecraftColor.put(Colors.DARK_BLUE, ChatColor.DARK_BLUE.toString()); // dark_blue
        ircToMinecraftColor.put(Colors.DARK_GREEN, ChatColor.DARK_GREEN.toString()); // dark_green
        ircToMinecraftColor.put(Colors.RED, ChatColor.RED.toString()); // red
        ircToMinecraftColor.put(Colors.BROWN, ChatColor.DARK_RED.toString()); // brown
        ircToMinecraftColor.put(Colors.PURPLE, ChatColor.DARK_PURPLE.toString()); // purple
        ircToMinecraftColor.put(Colors.OLIVE, ChatColor.GOLD.toString()); // olive
        ircToMinecraftColor.put(Colors.YELLOW, ChatColor.YELLOW.toString()); // yellow
        ircToMinecraftColor.put(Colors.GREEN, ChatColor.GREEN.toString()); // green
        ircToMinecraftColor.put(Colors.TEAL, ChatColor.DARK_AQUA.toString()); // teal
        ircToMinecraftColor.put(Colors.CYAN, ChatColor.AQUA.toString()); // cyan
        ircToMinecraftColor.put(Colors.BLUE, ChatColor.BLUE.toString()); // blue
        ircToMinecraftColor.put(Colors.MAGENTA, ChatColor.LIGHT_PURPLE.toString()); // magenta
        ircToMinecraftColor.put(Colors.DARK_GRAY, ChatColor.DARK_GRAY.toString()); // dark_gray
        ircToMinecraftColor.put(Colors.LIGHT_GRAY, ChatColor.GRAY.toString()); // light_gray
        minecraftToIrcColor.put(ChatColor.WHITE.toString(), Colors.WHITE); // black
        minecraftToIrcColor.put(ChatColor.BLACK.toString(), Colors.BLACK); // white
        minecraftToIrcColor.put(ChatColor.DARK_BLUE.toString(), Colors.DARK_BLUE); // dark_blue
        minecraftToIrcColor.put(ChatColor.DARK_GREEN.toString(), Colors.DARK_GREEN); // dark_green
        minecraftToIrcColor.put(ChatColor.RED.toString(), Colors.RED); // red
        minecraftToIrcColor.put(ChatColor.DARK_RED.toString(), Colors.BROWN); // brown
        minecraftToIrcColor.put(ChatColor.DARK_PURPLE.toString(), Colors.PURPLE); // purple
        minecraftToIrcColor.put(ChatColor.GOLD.toString(), Colors.OLIVE); // olive
        minecraftToIrcColor.put(ChatColor.YELLOW.toString(), Colors.YELLOW); // yellow
        minecraftToIrcColor.put(ChatColor.GREEN.toString(), Colors.GREEN); // green
        minecraftToIrcColor.put(ChatColor.DARK_AQUA.toString(), Colors.TEAL); // teal
        minecraftToIrcColor.put(ChatColor.AQUA.toString(), Colors.CYAN); // cyan
        minecraftToIrcColor.put(ChatColor.BLUE.toString(), Colors.BLUE); // blue
        minecraftToIrcColor.put(ChatColor.LIGHT_PURPLE.toString(), Colors.MAGENTA); // magenta
        minecraftToIrcColor.put(ChatColor.DARK_GRAY.toString(), Colors.DARK_GRAY); // dark_gray
        minecraftToIrcColor.put(ChatColor.GRAY.toString(), Colors.LIGHT_GRAY); // light_gray
    }

    /** Making default constructor private. */
    private ColorMap() { }

    /**
     * Convert a MineCraft color code to an IRC color code.
     * @param code
     *            A MineCraft color code
     * @return An IRC color code, black if the string did not match any code.
     */
    public static String chatToIrcColor(final String code) {
        String color = minecraftToIrcColor.get(code);
        if (color == null) {
            return Colors.BLACK;
        } else {
            return color;
        }
    }

    /**
     * Convert message from IRC to Minecraft Translates a colored irc message
     * into a colored minecraft message.
     * @param message
     *            the incoming irc message.
     * @return The converted minecraft string. TODO minecraft color doesn't span
     *         line wrap, force it by checking for last active color at
     *         linebreak.
     */
    public static String fromIrc(final String message) {
        // define IRC color pattern
        Matcher m = ircColorPattern.matcher(message);
        String result = "";
        int prev = 0;
        // Find all matches
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            // Add the unmatched parts to the result
            result += message.substring(prev, start);
            char digit1 = message.charAt(start + 1);
            char digit2 = 'a';
            if (start + 2 < message.length()) {
                digit2 = message.charAt(start + 2);
            }
            // We don't need to catch a numberformatexception
            // because the regular expression took care of that
            String color;
            if (Character.isDigit(digit2)) {
                color = "" + '\u0003' + digit1 + digit2;
            } else {
                color = "" + '\u0003' + "0" + digit1;
            }
            // Replace matched parts by the other color code
            result += ircToMinecraftColor.get(color);
            prev = end;
        }
        // Add the remaining string
        if (prev < message.length()) {
            result += message.substring(prev, message.length());
        }

        return result;
    }

    /**
     * Convert message from Minecraft to IRC Translates a colored minecraft
     * message into a colored irc message.
     * @param message
     *            the incoming minecraft message.
     * @return The converted irc string. TODO minecraft color doesn't span line
     *         wrap, force it by checking for last active color at linebreak.
     */
    public static String toIrc(final String message) {
        Matcher m = minecraftColorPattern.matcher(message);
        String result = "";
        int prev = 0;

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            // Add the unmatched parts to the result
            result += message.substring(prev, start);
            String code = "" + message.charAt(start) + message.charAt(start + 1);
            // System.out.println("Found code: " + code);
            result += minecraftToIrcColor.get(code);
            prev = end;
        }
        // Add the remaining string
        if (prev < message.length()) {
            result += message.substring(prev, message.length());
        }

        return result;
    }
}
