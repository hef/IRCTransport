/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author hef
 */
public class GroupName {
    private final IRCTransport plugin;
    
    GroupName(IRCTransport plugin)
    {
        this.plugin = plugin;
    }
    
    public String getGroupName(Player player)
    {
        List<?> groups = plugin.getConfig().getList("groups");
        for (Object i : groups) {
            if (i instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> linkedHashMapI = (LinkedHashMap<?, ?>) i;
            }
        }
        return null;
    }
}
