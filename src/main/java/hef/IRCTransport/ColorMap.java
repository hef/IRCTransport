package hef.IRCTransport;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.jibble.pircbot.Colors;

/**
 * @author hef
 *
 */
public class ColorMap {
	private HashMap<String, ChatColor> ircToMineColor; 
	ColorMap()
	{	
		this.ircToMineColor = new HashMap<String, ChatColor>();
		ircToMineColor.put( Colors.BLACK, ChatColor.WHITE );
		ircToMineColor.put( Colors.WHITE, ChatColor.BLACK );
		ircToMineColor.put( Colors.DARK_BLUE, ChatColor.DARK_BLUE );
		ircToMineColor.put( Colors.DARK_GREEN, ChatColor.DARK_GREEN );
		ircToMineColor.put( Colors.RED, ChatColor.RED );
		ircToMineColor.put( Colors.BROWN, ChatColor.DARK_RED );
		ircToMineColor.put( Colors.PURPLE, ChatColor.DARK_PURPLE );
		ircToMineColor.put( Colors.OLIVE, ChatColor.GOLD );
		ircToMineColor.put( Colors.YELLOW, ChatColor.YELLOW );
		ircToMineColor.put( Colors.GREEN, ChatColor.GREEN );
		ircToMineColor.put( Colors.TEAL, ChatColor.DARK_AQUA );
		ircToMineColor.put( Colors.CYAN, ChatColor.AQUA );
		ircToMineColor.put( Colors.BLUE, ChatColor.BLUE );
		ircToMineColor.put( Colors.MAGENTA, ChatColor.LIGHT_PURPLE );
		ircToMineColor.put( Colors.DARK_GRAY, ChatColor.DARK_GRAY );
		ircToMineColor.put( Colors.LIGHT_GRAY, ChatColor.GRAY );
	}
	
	/** Convert message from irc to minecraft
	 * Translates a colored irc message into a colored minecraft message.
	 * @param message the incoming irc message.
	 * @return The converted minecraft string.
	 */
	public String fromIrc(String message)
	{
		char[] messageArray = message.toCharArray();
		for(int i = 0; i < message.length(); ++i)
		{
			if(ircToMineColor.containsKey( messageArray[i] ))
			{
				messageArray[i]= ircToMineColor.get(messageArray[i]).toString().charAt(0);
			}
		}
		return new String(messageArray);
	}
}
