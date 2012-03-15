/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Gets the group name of a player from config.
 */
public class GroupName {
    /** A reference to the IRCTranport instance. */
    private final IRCTransport plugin;
    
    /**
     * Sets up the Object.
     * @param plugin used to get config file.
     */
    GroupName(IRCTransport plugin) {
        this.plugin = plugin;
    }
    
    /**
     * This class makes certain assumptions.
     * It assumes that ConfigurationSection.getKeys comes out in order.
     * @param player the player we are checking permissions on.
     * @return The name of the group the player is in.
     */
    public String getGroupName(Player player) {
        ConfigurationSection groupsCS= plugin.getConfig().getConfigurationSection("groups");
        for(String permission: groupsCS.getKeys(false)) {
            if(player.hasPermission(permission)) {
                return groupsCS.getString(permission);
            }        
        }
        return "";
    }
}
