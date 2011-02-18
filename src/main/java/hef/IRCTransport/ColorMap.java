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
	private static final HashMap<String, String> ircToMineColor = new HashMap<String, String>(); 
	static
	{	
		ircToMineColor.put( Colors.BLACK, ChatColor.WHITE.toString() );
		ircToMineColor.put( Colors.WHITE, ChatColor.BLACK.toString() );
		ircToMineColor.put( Colors.DARK_BLUE, ChatColor.DARK_BLUE.toString() );
		ircToMineColor.put( Colors.DARK_GREEN, ChatColor.DARK_GREEN.toString() );
		ircToMineColor.put( Colors.RED, ChatColor.RED.toString() );
		ircToMineColor.put( Colors.BROWN, ChatColor.DARK_RED.toString() );
		ircToMineColor.put( Colors.PURPLE, ChatColor.DARK_PURPLE.toString() );
		ircToMineColor.put( Colors.OLIVE, ChatColor.GOLD.toString() );
		ircToMineColor.put( Colors.YELLOW, ChatColor.YELLOW.toString() );
		ircToMineColor.put( Colors.GREEN, ChatColor.GREEN.toString() );
		ircToMineColor.put( Colors.TEAL, ChatColor.DARK_AQUA.toString() );
		ircToMineColor.put( Colors.CYAN, ChatColor.AQUA.toString() );
		ircToMineColor.put( Colors.BLUE, ChatColor.BLUE.toString() );
		ircToMineColor.put( Colors.MAGENTA, ChatColor.LIGHT_PURPLE.toString() );
		ircToMineColor.put( Colors.DARK_GRAY, ChatColor.DARK_GRAY.toString() );
		ircToMineColor.put( Colors.LIGHT_GRAY, ChatColor.GRAY.toString() );
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
				messageBytes[i]= ircToMineColor.get(messageBytes[i]).toString().charAt(0);
			}
			
		}
		return new String(messageBytes);
	}
}
