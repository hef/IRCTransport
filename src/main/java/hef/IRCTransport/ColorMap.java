package hef.IRCTransport;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.jibble.pircbot.Colors;

/**
 * @author hef
 *
 */
public class ColorMap {
	//TODO: char to char mapping
	private static ArrayList<String> minecraftColor = new ArrayList<String>(16);
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
	
	/** Convert message from IRC to Minecraft
	 * Translates a colored irc message into a colored minecraft message.
	 * @param message the incoming irc message.
	 * @return The converted minecraft string.
	 * TODO minecraft color doesn't span line wrap, force it by checking for last active color at linebreak.
	 */
	public static String fromIrc(String message)
	{
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        char digit1;
        char digit2;
        while (i < message.length())
        {
        	digit1='\0';
        	digit2='\0';
        	//if char is irc color code
        	if (message.charAt(i) == '\u0003')
        	{
        		++i;
        		//if char is digit x in x or xy
        		if( i < message.length() && Character.isDigit(message.charAt(i)))
        		{
        			digit1 = message.charAt(i);
        			++i;
        			//if char is digit y in xy
        			if( i< message.length() && Character.isDigit(message.charAt(i)))
        			{
        				digit2 = message.charAt(i);
        				++i;
        			}
        			//we have a color code and at least 1 digit, check for background color (,x) or (,xy)
        			if( i < message.length() && message.charAt(i) == ',')
        			{
        				++i;
        				if( i < message.length())
        				{
        					if( Character.isDigit(message.charAt(i)) )
        					{
        						++i;
        						if(i < message.length() && Character.isDigit(message.charAt(i)))
        						{
        							++i;
        						}
        					}
        					else
        					{
        						//a comma was detected, but there was no color code.
        						//put the comma back
        						--i;
        					}
        				}
        			}
        			//deal with digit1 and digit2
        			String colorcode = "";
        			if(digit1 != '\0')
        				colorcode += Character.toString(digit1);
        			if(digit2 != '\0')
        				colorcode += Character.toString(digit2);
        			if(digit1 != '\0')
        			{
        				int ircCode = Integer.parseInt(colorcode);
        				String mineCode = minecraftColor.get(ircCode);
        				buffer.append(mineCode);
        			}
        		}
        	}
        	buffer.append(message.charAt(i));
			++i;
        }
        return buffer.toString();
	}
}
