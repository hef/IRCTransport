package hef.IRCTransport;

import java.util.HashMap;

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
    private HashMap<Player, IrcAgent> bots;
    
    public IRCTransportPlayerListener(IRCTransport instance) {
    	this.bots = instance.getBots();
        plugin = instance;
    }
    public void onPlayerChat(PlayerChatEvent event)
    {
    	IrcAgent bot = this.bots.get(event.getPlayer());
    	bot.sendMessage(event.getMessage());
    	//prevent messages from being displayed twice.
    	event.setCancelled(true);
    }
    public void onPlayerJoin(PlayerEvent event) 
    {
    	this.bots.put(event.getPlayer(), new IrcAgent(plugin, event.getPlayer()));
    }
    public void onPlayerQuit(PlayerEvent event)
    {
    	this.bots.get(event.getPlayer()).shutdown();
    	this.bots.remove(event.getPlayer());
    }
}

