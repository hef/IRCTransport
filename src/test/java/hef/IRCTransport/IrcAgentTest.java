/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import org.bukkit.entity.Player;
import org.jibble.pircbot.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author hef
 */
public class IrcAgentTest {
        IRCTransport mockIRCTransport;
        Player mockPlayer;
        IrcAgent agent;
    
    public IrcAgentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        mockIRCTransport = mock(IRCTransport.class);
        mockPlayer = mock(Player.class);
        agent = new IrcAgent(mockIRCTransport, mockPlayer);
    }
    
    @After
    public void tearDown() {
        mockIRCTransport = null;
        mockPlayer = null;
        agent = null;
    }

    /**
     * Test of getActiveChannel method, of class IrcAgent.
     */
    @Test
    public void testGetActiveChannel() {
        System.out.println("getActiveChannel");
        IrcAgent instance = null;
        String expResult = "";
        String result = instance.getActiveChannel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlayer method, of class IrcAgent.
     */
    @Test
    public void testGetPlayer() {
        Player expResult = mockPlayer;
        Player result = agent.getPlayer();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPlugin method, of class IrcAgent.
     */
    @Test
    public void testGetPlugin() {
        IRCTransport expResult = mockIRCTransport;
        IRCTransport result = agent.getPlugin();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSettings method, of class IrcAgent.
     */
    @Test
    public void testGetSettings() {
        System.out.println("getSettings");
        IrcAgent instance = null;
        AgentSettings expResult = null;
        AgentSettings result = instance.getSettings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isShuttingDown method, of class IrcAgent.
     */
    @Test
    public void testIsShuttingDown() {
        System.out.println("isShuttingDown");
        IrcAgent instance = null;
        boolean expResult = false;
        boolean result = instance.isShuttingDown();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of log method, of class IrcAgent.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        String line = "";
        IrcAgent instance = null;
        instance.log(line);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of names method, of class IrcAgent.
     */
    @Test
    public void testNames_0args() {
        System.out.println("names");
        IrcAgent instance = null;
        instance.names();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of names method, of class IrcAgent.
     */
    @Test
    public void testNames_String() {
        System.out.println("names");
        String channel = "";
        IrcAgent instance = null;
        instance.names(channel);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onAction method, of class IrcAgent.
     */
    @Test
    public void testOnAction() {
        System.out.println("onAction");
        String sender = "";
        String login = "";
        String hostname = "";
        String target = "";
        String action = "";
        IrcAgent instance = null;
        instance.onAction(sender, login, hostname, target, action);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onConnect method, of class IrcAgent.
     */
    @Test
    public void testOnConnect() {
        System.out.println("onConnect");
        IrcAgent instance = null;
        instance.onConnect();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onDisconnect method, of class IrcAgent.
     */
    @Test
    public void testOnDisconnect() {
        System.out.println("onDisconnect");
        IrcAgent instance = null;
        instance.onDisconnect();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onErrorMessage method, of class IrcAgent.
     */
    @Test
    public void testOnErrorMessage() {
        System.out.println("onErrorMessage");
        String channel = "";
        String message = "";
        IrcAgent instance = null;
        instance.onErrorMessage(channel, message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onJoin method, of class IrcAgent.
     */
    @Test
    public void testOnJoin() {
        System.out.println("onJoin");
        String channel = "";
        String sender = "";
        String login = "";
        String hostname = "";
        IrcAgent instance = null;
        instance.onJoin(channel, sender, login, hostname);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onKick method, of class IrcAgent.
     */
    @Test
    public void testOnKick() {
        System.out.println("onKick");
        String channel = "";
        String kickerNick = "";
        String kickerLogin = "";
        String kickerHostname = "";
        String recipientNick = "";
        String reason = "";
        IrcAgent instance = null;
        instance.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onMessage method, of class IrcAgent.
     */
    @Test
    public void testOnMessage() {
        System.out.println("onMessage");
        String channel = "";
        String sender = "";
        String login = "";
        String hostname = "";
        String message = "";
        IrcAgent instance = null;
        instance.onMessage(channel, sender, login, hostname, message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onNickChange method, of class IrcAgent.
     */
    @Test
    public void testOnNickChange() {
        System.out.println("onNickChange");
        String oldNick = "";
        String login = "";
        String hostname = "";
        String newNick = "";
        IrcAgent instance = null;
        instance.onNickChange(oldNick, login, hostname, newNick);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onPart method, of class IrcAgent.
     */
    @Test
    public void testOnPart() {
        System.out.println("onPart");
        String channel = "";
        String sender = "";
        String login = "";
        String hostname = "";
        IrcAgent instance = null;
        instance.onPart(channel, sender, login, hostname);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onPrivateMessage method, of class IrcAgent.
     */
    @Test
    public void testOnPrivateMessage() {
        System.out.println("onPrivateMessage");
        String sender = "";
        String login = "";
        String hostname = "";
        String message = "";
        IrcAgent instance = null;
        instance.onPrivateMessage(sender, login, hostname, message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onQuit method, of class IrcAgent.
     */
    @Test
    public void testOnQuit() {
        System.out.println("onQuit");
        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";
        String reason = "";
        IrcAgent instance = null;
        instance.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onServerResponse method, of class IrcAgent.
     */
    @Test
    public void testOnServerResponse() {
        System.out.println("onServerResponse");
        int code = 0;
        String response = "";
        IrcAgent instance = null;
        instance.onServerResponse(code, response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onTopic method, of class IrcAgent.
     */
    @Test
    public void testOnTopic() {
        System.out.println("onTopic");
        String channel = "";
        String topic = "";
        String setBy = "";
        long date = 0L;
        boolean changed = false;
        IrcAgent instance = null;
        instance.onTopic(channel, topic, setBy, date, changed);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of onUserList method, of class IrcAgent.
     */
    @Test
    public void testOnUserList() {
        System.out.println("onUserList");
        String channel = "";
        User[] users = null;
        IrcAgent instance = null;
        instance.onUserList(channel, users);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveSettings method, of class IrcAgent.
     */
    @Test
    public void testSaveSettings() {
        System.out.println("saveSettings");
        IrcAgent instance = null;
        instance.saveSettings();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendAction method, of class IrcAgent.
     */
    @Test
    public void testSendAction() {
        System.out.println("sendAction");
        String action = "";
        IrcAgent instance = null;
        instance.sendAction(action);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendMessage method, of class IrcAgent.
     */
    @Test
    public void testSendMessage() {
        System.out.println("sendMessage");
        String message = "";
        IrcAgent instance = null;
        instance.sendMessage(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setActiveChannel method, of class IrcAgent.
     */
    @Test
    public void testSetActiveChannel() {
        System.out.println("setActiveChannel");
        String channel = "";
        IrcAgent instance = null;
        instance.setActiveChannel(channel);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNick method, of class IrcAgent.
     */
    @Test
    public void testSetNick() {
        System.out.println("setNick");
        String name = "";
        IrcAgent instance = null;
        instance.setNick(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSettings method, of class IrcAgent.
     */
    @Test
    public void testSetSettings() {
        System.out.println("setSettings");
        AgentSettings agentSettings = null;
        IrcAgent instance = null;
        instance.setSettings(agentSettings);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTopic method, of class IrcAgent.
     */
    @Test
    public void testSetTopic() {
        System.out.println("setTopic");
        String topic = "";
        IrcAgent instance = null;
        instance.setTopic(topic);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of shutdown method, of class IrcAgent.
     */
    @Test
    public void testShutdown() {
        System.out.println("shutdown");
        IrcAgent instance = null;
        instance.shutdown();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of topic method, of class IrcAgent.
     */
    @Test
    public void testTopic() {
        System.out.println("topic");
        IrcAgent instance = null;
        instance.topic();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of whois method, of class IrcAgent.
     */
    @Test
    public void testWhois() {
        System.out.println("whois");
        String nick = "";
        IrcAgent instance = null;
        instance.whois(nick);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
