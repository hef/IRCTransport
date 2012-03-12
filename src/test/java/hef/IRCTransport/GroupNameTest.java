/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import java.io.InputStream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hef
 */
public class GroupNameTest {
    YamlConfiguration config;
    
    public GroupNameTest() {
    }
    
    @Before
    public void setUp() {
        ClassLoader cldr = Thread.currentThread().getContextClassLoader();
        InputStream ymlStream = cldr.getResourceAsStream("groups.yml");       
        config = YamlConfiguration.loadConfiguration(ymlStream);
    }
    
    @After
    public void tearDown() {
        config = null;
    }
    
    @Test
    public void TestGetGroupName()
    {
        
        fail("Not Yet Implemented");
    }
}
