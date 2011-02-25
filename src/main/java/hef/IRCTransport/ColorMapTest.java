/**
 * 
 */
package hef.IRCTransport;

import static org.junit.Assert.*;

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
		String ircRed = "string is " + Colors.RED.toString() + "red";
		String minecraftRed = "string is " + ChatColor.RED + "red";
		
		String result = ColorMap.fromIrc(ircRed);
		assertTrue(minecraftRed.equals(result));
		}

}
