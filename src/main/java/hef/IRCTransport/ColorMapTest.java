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
		
		String input1 = "string is " + Colors.RED.toString() + "red";
		String expected1 = "string is " + ChatColor.RED + "red";
		String output1 = ColorMap.fromIrc(input1);
		assertTrue(expected1.equals(output1));
		
		String input2 = "string is "+'\u0003'+"4red";
		String expected2 = "string is " + ChatColor.RED + "red";
		String output2 = ColorMap.fromIrc(input2);
		assertTrue(expected2.equals(output2));
		
		// test line ending with a color
		String input3 = "string is " + Colors.RED.toString() + "red" + Colors.WHITE.toString();
		String expected3 = "string is " + ChatColor.RED + "red" + ChatColor.WHITE;
		String output3 = ColorMap.fromIrc(input3);
		assertTrue(expected3.equals(output3));
	}
	
	/**
	 * Test method for {@link hef.IRCTransport.ColorMap#toIrc(java.lang.String)}.
	 */
	@Test
	public final void testToIrc() {
		String ircRed = "string is " + Colors.RED.toString() + "red";
		String minecraftRed = "string is " + ChatColor.RED + "red";
		String result = ColorMap.toIrc(minecraftRed);
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
