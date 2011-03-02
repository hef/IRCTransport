/**
 * 
 */
package hef.IRCTransport;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jibble.pircbot.Colors;

import org.bukkit.ChatColor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author hef
 *
 */
public class ColorMapTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link hef.IRCTransport.ColorMap#fromIrc(java.lang.String)}.
	 */
	@Test
	public final void testFromIrc() {
		List<String> ircLines = new ArrayList<String>();
		ircLines.add("string is " + Colors.RED.toString() + "red");
		ircLines.add("string is "+'\u0003'+"4red");
		ircLines.add("string is " + Colors.RED.toString() + "red" + Colors.WHITE.toString()); // test line ending with a color
		
		List<String> mcLines = new ArrayList<String>();
		mcLines.add("string is " + ChatColor.RED + "red");
		mcLines.add("string is " + ChatColor.RED + "red");
		mcLines.add("string is " + ChatColor.RED + "red" + ChatColor.WHITE);
		
		for(int i = 0; i < ircLines.size(); i++) {
			assertTrue(mcLines.get(i).equals(ColorMap.fromIrc(ircLines.get(i))));
		}
		
	}
	
	/**
	 * Test method for {@link hef.IRCTransport.ColorMap#toIrc(java.lang.String)}.
	 */
	@Test
	public final void testToIrc() {
		String ircRed = "string is " + Colors.RED.toString() + "red";
		//System.out.println(ircRed);
		String minecraftRed = "string is " + ChatColor.RED + "red";
		//System.out.println(minecraftRed);
		//System.out.println(ChatColor.BLACK);
		//System.out.println(ChatColor.AQUA);
		
		String result = ColorMap.toIrc(minecraftRed);
		//System.out.println(result);
		assertTrue(ircRed.equals(result));
	}
	
	
	/**
	 * Test method for {@link hef.IRCTransport.ColorMap#chatToIrcColor(java.lang.String)}.
	 */
	@Test
	public final void testChatToIrcColor() {
		assertTrue(ColorMap.chatToIrcColor("wrong").equals(Colors.BLACK.toString()));
		assertTrue(ColorMap.chatToIrcColor(ChatColor.WHITE.toString()).equals(Colors.WHITE.toString()));
	}

}
