package com.bukkit.hef.IRCTransport;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;


/**
 * Handle events for all Player related events
 * @author hef
 */
public class IRCTransportPlayerListener extends PlayerListener {
    private final IRCTransport plugin;

    public IRCTransportPlayerListener(IRCTransport instance) {
        plugin = instance;
        //establish list of players
        Player[] players = instance.getServer().getOnlinePlayers();
        for(Player player: players)
        {
        	plugin.bots.put(player, new PlayerBot(player));
        }
    }
    public void onPlayerChat(PlayerChatEvent event)
    {
    	plugin.bots.get(event.getPlayer()).sendMessage("#minecraft", event.getMessage());    
    }
    public void onPlayerJoin(PlayerEvent event) 
    {
    	plugin.bots.put(event.getPlayer(), new PlayerBot(event.getPlayer()));
    }
    public void onPlayerQuit(PlayerEvent event)
    {
    	plugin.bots.get(event.getPlayer()).disconnect();
    	plugin.bots.remove(event.getPlayer());
    }
    
}

