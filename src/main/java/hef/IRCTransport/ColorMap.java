package hef.IRCTransport;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.jibble.pircbot.Colors;

/**
 * @author hef
 *
 */
public class ColorMap {
	//TODO: char to char mapping
	private static HashMap<Character, Character> ircToMineColor = new HashMap<Character, Character>(); 
	static
	{	
		ircToMineColor.put( Colors.BLACK.charAt(0), ChatColor.WHITE.toString().charAt(0) );
		ircToMineColor.put( Colors.WHITE.charAt(0), ChatColor.BLACK.toString().charAt(0) );
		ircToMineColor.put( Colors.DARK_BLUE.charAt(0), ChatColor.DARK_BLUE.toString().charAt(0) );
		ircToMineColor.put( Colors.DARK_GREEN.charAt(0), ChatColor.DARK_GREEN.toString().charAt(0) );
		ircToMineColor.put( Colors.RED.charAt(0), ChatColor.RED.toString().charAt(0) );
		ircToMineColor.put( Colors.BROWN.charAt(0), ChatColor.DARK_RED.toString().charAt(0) );
		ircToMineColor.put( Colors.PURPLE.charAt(0), ChatColor.DARK_PURPLE.toString().charAt(0) );
		ircToMineColor.put( Colors.OLIVE.charAt(0), ChatColor.GOLD.toString().charAt(0) );
		ircToMineColor.put( Colors.YELLOW.charAt(0), ChatColor.YELLOW.toString().charAt(0) );
		ircToMineColor.put( Colors.GREEN.charAt(0), ChatColor.GREEN.toString().charAt(0) );
		ircToMineColor.put( Colors.TEAL.charAt(0), ChatColor.DARK_AQUA.toString().charAt(0) );
		ircToMineColor.put( Colors.CYAN.charAt(0), ChatColor.AQUA.toString().charAt(0) );
		ircToMineColor.put( Colors.BLUE.charAt(0), ChatColor.BLUE.toString().charAt(0) );
		ircToMineColor.put( Colors.MAGENTA.charAt(0), ChatColor.LIGHT_PURPLE.toString().charAt(0) );
		ircToMineColor.put( Colors.DARK_GRAY.charAt(0), ChatColor.DARK_GRAY.toString().charAt(0) );
		ircToMineColor.put( Colors.LIGHT_GRAY.charAt(0), ChatColor.GRAY.toString().charAt(0) );
	}
	
	/** Convert message from IRC to Minecraft
	 * Translates a colored irc message into a colored minecraft message.
	 * @param message the incoming irc message.
	 * @return The converted minecraft string.
	 * TODO minecraft color doesn't span line wrap, force it by checking for last active color at linebreak.
	 */
	public static String fromIrc(String message)
	{
		char[] messageBytes=message.toCharArray();
		for(int i = 0; i < message.length(); ++i)
		{
			//search an replace irc color byte with minecraft color byte
			if(ircToMineColor.containsKey( Character.toString(messageBytes[i]) ))
			{
				messageBytes[i]= ircToMineColor.get(messageBytes[i]);
			}
			
		}
		return new String(messageBytes);
	}
}
