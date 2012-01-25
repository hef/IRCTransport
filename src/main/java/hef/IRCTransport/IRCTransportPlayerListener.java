package hef.IRCTransport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events.
 */
 public class IRCTransportPlayerListener extends PlayerListener {
    /** Maps to retrieve associated IrcAggent from player. */
    private TIntObjectHashMap<IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;
    /** Logger object. */
    private Logger log;

    /**
     * @param instance A reference to the plugin.
     */
    public IRCTransportPlayerListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
        log = instance.getServer().getLogger();
    }

    @Override
    public void onPlayerChat(final PlayerChatEvent event) {
        IrcAgent bot = this.bots.get(event.getPlayer().getEntityId());
        if (bot.isConnected()) {
            bot.sendMessage(event.getMessage());
            // prevent messages from being displayed twice.
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        IrcAgent agent;
        
        try {
        	agent = new IrcAgent(plugin, player);
        } catch (Exception e) {
        	return;
        }
        
        agent.getListenerManager().addListener(plugin.getListener());
        this.bots.put(playerID, agent);
        
        /* Added until pircbotx onConnect() works */
        if(agent.getChannelsNames().isEmpty())
        {
        	log.warning("[IRCTransport] Failed to call onConnect(). Joining default channel...");
        	agent.getPlayer().setDisplayName(agent.getNick());
        	agent.getSettings().setIrcNick(agent.getNick());

            boolean bSuppressNames = plugin.getConfig().getBoolean("suppress.initial_userlist", false);
            boolean bSuppressTopic = plugin.getConfig().getBoolean("suppress.initial_topic", false);
            List<?> channelData = plugin.getConfig().getList("default.channels");

            for (Object i : channelData) {
                if (i instanceof LinkedHashMap) {
                    LinkedHashMap<?, ?> linkedHashMapI = (LinkedHashMap<?, ?>) i;

                    String channelName = (String) linkedHashMapI.get("channel");
                    if (channelName != null) {
                        if (bSuppressNames) {
                        	agent.getSuppressNames().add(agent.getChannel(channelName));
                        }
                        if (bSuppressTopic) {
                        	agent.getSuppressTopic().add(agent.getChannel(channelName));
                        }
                        String key = (String) linkedHashMapI.get("key");
                        if (key != null) {
                        	agent.joinChannel(channelName, key);
                        } else {
                        	agent.joinChannel(channelName);
                        }
                    }
                } else {
                    log.log(Level.WARNING, "Object: {0} is a {1}", new Object[]{i.toString(), i.getClass().toString()});
                }
            }
        }
        log.info(String.format("Created agent for Player ID: %d name: %s", playerID, player.getName()));
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        this.bots.get(playerID).shutdown();
        this.bots.remove(playerID);
        log.info(String.format("Removed agent for Player ID: %d name: %s", playerID, player.getName()));
    }
}
