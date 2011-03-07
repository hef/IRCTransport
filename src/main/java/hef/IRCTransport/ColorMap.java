package hef.IRCTransport;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/**
 * @author hef
 *
 */
public class ColorMap {
	//TODO: char to char mapping
	private static ArrayList<String> minecraftColor = new ArrayList<String>(16);
	private static Pattern minecraftColorPattern = Pattern.compile('\u00A7'+"[0-9a-z]");
	private static Pattern ircColorPattern = Pattern.compile('\u0003'+"[0-9]{1,2}(?:,[0-9]{1,2})?");
	static
	{	
		minecraftColor.add(0, ChatColor.WHITE.toString());			//black
		minecraftColor.add(1, ChatColor.BLACK.toString());  		//white
		minecraftColor.add(2, ChatColor.DARK_BLUE.toString()); 		//dark_blue
		minecraftColor.add(3, ChatColor.DARK_GREEN.toString());		//dark_green
		minecraftColor.add(4, ChatColor.RED.toString());			//red
		minecraftColor.add(5, ChatColor.DARK_RED.toString());		//brown
		minecraftColor.add(6, ChatColor.DARK_PURPLE.toString());	//purple
		minecraftColor.add(7, ChatColor.GOLD.toString());			//olive
		minecraftColor.add(8, ChatColor.YELLOW.toString());			//yellow
		minecraftColor.add(9, ChatColor.GREEN.toString());			//green
		minecraftColor.add(10, ChatColor.DARK_AQUA.toString());		//teal
		minecraftColor.add(11, ChatColor.AQUA.toString());			//cyan
		minecraftColor.add(12, ChatColor.BLUE.toString());			//blue
		minecraftColor.add(13, ChatColor.LIGHT_PURPLE.toString());	//magenta
		minecraftColor.add(14, ChatColor.DARK_GRAY.toString());		//dark_gray
		minecraftColor.add(15, ChatColor.GRAY.toString());			//light_gray
	}
	
	/**
	 * Convert a MineCraft color code to an IRC color code
	 * @param code A MineCraft color code
	 * @return An IRC color code, black if the string did not match any code.
	 */
	public static String chatToIrcColor(String code)
	{
		for(int i = 0; i < minecraftColor.size() ;  i++)
		{
			if(minecraftColor.get(i).equals(code))
			{
				String result = "\u0003";
				if(i < 10) result += "0";
				return result + i;
			}
		}
		return "\u000301"; // Let's just make it black by default, good for IRC 
	}
	
	/**
	 * Convert message from Minecraft to IRC
	 * Translates a colored minecraft message into a colored irc message.
	 * @param message the incoming minecraft message.
	 * @return The converted irc string.
	 * TODO minecraft color doesn't span line wrap, force it by checking for last active color at linebreak.
	 */
	public static String toIrc(String message)
	{
		Matcher m = minecraftColorPattern.matcher(message);
		String result = "";
		int prev = 0;
		
		while(m.find())	{
			int start = m.start();
			int end = m.end();
			// Add the unmatched parts to the result
			result += message.substring(prev, start);
			String code = ""+message.charAt(start) + message.charAt(start+1);
			//System.out.println("Found code: " + code);
			result += chatToIrcColor(code);
			prev = end;
		}
		// Add the remaining string
		if(prev < message.length())
			result += message.substring(prev, message.length());
		
		return result;
	}
	
	/** Convert message from IRC to Minecraft
	 * Translates a colored irc message into a colored minecraft message.
	 * @param message the incoming irc message.
	 * @return The converted minecraft string.
	 * TODO minecraft color doesn't span line wrap, force it by checking for last active color at linebreak.
	 */
	public static String fromIrc(String message)
	{
		// define IRC color pattern
		Matcher m = ircColorPattern.matcher(message);
		String result = "";
		int prev = 0;
		// Find all matches
		while(m.find())	{
			int start = m.start();
			int end = m.end();
			// Add the unmatched parts to the result
			result += message.substring(prev, start);
			char digit1 = message.charAt(start+1);
			char digit2 = 'a';
			if(start+2 < message.length())
				digit2 = message.charAt(start+2);
			// We don't need to catch a numberformatexception
			// because the regular expression took care of that
			String color = "" + digit1 + (Character.isDigit(digit2) ? digit2 : "");
			int code = Integer.parseInt(color);
			// Replace matched parts by the other color code
			result += minecraftColor.get(code);
			prev = end;
		}
		// Add the remaining string
		if(prev < message.length())
			result += message.substring(prev, message.length());
			
		return result;
	}
}
