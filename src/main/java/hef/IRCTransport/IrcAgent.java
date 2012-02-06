package hef.IRCTransport;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import org.bukkit.entity.Player;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;

/**
 * Represent a player to an IRC server. Every Bukkit player should have one!
 * Every agent will have channels, a player, and an active channel.
 */
public class IrcAgent extends PircBotX {

    /** Used to send message to the console. */
    private static final Logger LOG = Logger.getLogger("Minecraft");
    /** The active channel. */
    private Channel activeChannel;
    /** A reference to the Bukkit Player object. */
    private Player player;
    /** A reference to the IRCTransport plugin instance. */
    private final IRCTransport plugin;
    /** The settings object associated with this agent. */
    private AgentSettings settings;
    /** Flag to indicate we should not reconnect. */
    private boolean shuttingDown;
    /**
     * A set of channels to suppress onUserList. This is used to hide initial
     * join messages.
     */
    private HashSet<Channel> suppressNames = new HashSet<Channel>();
    ;
    /**
     * A set of channels to suppress Topic message. This is used to hid initial
     * join messages.
     */
    private HashSet<Channel> suppressTopic = new HashSet<Channel>();

    /**
     * Agent Constructor.
     * @param instance Reference to plugin instance.
     * @param bukkitPlayer Reference to Bukkit Player
     * @throws Exception SSL failure
     */
    public IrcAgent(final IRCTransport instance, final Player bukkitPlayer) throws Exception {
        this.plugin = instance;
        this.player = bukkitPlayer;
        this.shuttingDown = false;
        setLogin(String.format("%s", player.getEntityId()));
        super.setAutoNickChange(true);

        // init player settings
        setSettings(plugin.getDatabase().find(AgentSettings.class, player.getName()));
        if (null == getSettings()) {
            setSettings(new AgentSettings(player));
            String prefix = plugin.getConfig().getString("default.prefix", "");
            String suffix = plugin.getConfig().getString("default.suffix", "");
        	int ircnicksize = plugin.getConfig().getInt("server.nicksize", 15);
        	String nick = String.format("%s%s%s", prefix, player.getName(), suffix);
        	if (nick.length() > ircnicksize)
        		nick = nick.substring(0, ircnicksize);
            getSettings().setIrcNick(nick);
            
        } else {
            String format = "Player '%s' using persistent IRC nick '%s'";
            String name = player.getName();
            String nick = getSettings().getIrcNick();
            LOG.log(Level.INFO, String.format(format, name, nick));
        }
        setNick(getSettings().getIrcNick());
        this.getListenerManager().addListener(new IrcListener(instance));
        Connect connection; 
        connection = new Connect(this);
        connection.run();
        if(connection.exception)
        	throw new Exception("Failed to connect");
    }

    /**
     * Connect the agent. Don't call this directly, call `new
     * Connect(this).run()` instead.
     * @throws IOException If it was not possible to connect to the server.
     * @throws IrcException If the server would not let us join it.
     * @throws Exception SSL failure
     */
    public void connect() throws IOException, IrcException, Exception {
        String address = getPlugin().getConfig().getString("server.address");
        int port = getPlugin().getConfig().getInt("server.port");
        String password = getPlugin().getConfig().getString("server.password");

        SocketFactory socketFactory = null;

        if (getPlugin().getConfig().getBoolean("server.ssl.enabled", false)) {
        	if (getPlugin().getConfig().getBoolean("server.ssl.trust", false)) {
        		socketFactory = new UtilSSLSocketFactory().trustAllCertificates();
        	} else {
        		socketFactory = new UtilSSLSocketFactory();
        	}
        }

        if (!isConnected()) {
            if (getServer() == null) {
            	try {
            		connect(address, port, password, socketFactory);
            	} catch (Exception e) {
            		LOG.log(Level.SEVERE, "[IRCTransport] Unable to connect. Disabling plugin.");
            		plugin.getPluginLoader().disablePlugin(plugin);
            		throw new Exception("Connect Error");
            	}
            } else {
                reconnect();
            }
        }
        this.joinChannel(plugin.getConfig().getString("autojoin"));
    }

    /**
     * Fetch the active channel. The active channel is the channel that a player
     * will talk in if they don't specify a channel.
     * @return a string with the active channel name.
     */
    public Channel getActiveChannel() {
        return this.activeChannel;
    }

    /**
     * Get the Player.
     * @return Reference to Bukkit Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The IRCTransport plugin instance.
     * @return a reference to the IRC plugin.
     */
    public IRCTransport getPlugin() {
        return plugin;
    }

    /**
     * @return the settings
     */
    public AgentSettings getSettings() {
        return settings;
    }

    /**
     * Shutting Down Flag Useful for preventing reconnection measures.
     * @return Is the agent shutting down?
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Log stuff. This method only logs to INFO if the Verbose flags is set.
     * @param line The line you want logged to console.
     */
    @Override
    public void log(final String line) {
        if (plugin.getConfig().getBoolean("verbose")) {
            LOG.log(Level.INFO, line);
        }
    }

    /** call names(activechannel). */
    protected void names() {
        names(activeChannel);
    }

    /**
     * Get a list of playernames from a channel. removes muteNames flag for
     * channel.
     * @param activeChannel2 The channel to list names from.
     */
    protected void names(final Channel activeChannel2) {
        getSuppressNames().remove(activeChannel2);
        sendRawLine("NAMES " + activeChannel2.getName());
    }

    /** Save agent settings to persistent data store. */
    protected void saveSettings() {
        plugin.getDatabase().save(getSettings());
    }

    /**
     * Action sender. triggers when player sends a /me
     * @param action The content of the action.
     */
    public void sendAction(final String action) {
    	String actiontr = action;
    	String trans = plugin.getConfig().getString("translations." + action, "");
    	if (! trans.equals("")) {
    		actiontr = trans;
    	}
        sendAction(activeChannel, actiontr);
        getPlayer().sendMessage(String.format("* %s %s", /*activeChannel.getName(),*/ getPlayer().getDisplayName(), actiontr));
    }

    /**
     * Sends a message to the active channel.
     * @param message The message to send
     */
    public void sendMessage(final String message) {
        sendMessage(activeChannel, message);
        if (isConnected()) {
        	String formattedMessage = plugin.getConfig().getString("messages.chat-ingame");
        	String group = IRCTransport.permissionHandler.getGroupProperName(player.getWorld().getName(), player.getName());
        	String prefix = IRCTransport.permissionHandler.getGroupRawPrefix(player.getWorld().getName(), group);
        	String suffix = IRCTransport.permissionHandler.getGroupRawSuffix(player.getWorld().getName(), group);
        	formattedMessage = formattedMessage.replace("${GROUP}", group);
        	formattedMessage = formattedMessage.replace("${PREFIX}", prefix);
        	formattedMessage = formattedMessage.replace("${SUFFIX}", suffix);
        	formattedMessage = formattedMessage.replace("${NICK}", getPlayer().getDisplayName());
            formattedMessage = formattedMessage.replace("${MESSAGE}", message);
            getPlayer().sendMessage(formattedMessage.replace("&", "\u00A7"));
        }
    }

    /**
     * Change active channel.
     * @param channel The channel to make the active one.
     */
    public void setActiveChannel(final Channel channel) {
        this.activeChannel = channel;
    }

    /**
     * Set name to attempt to use at login This function is not the same as
     * changeNick(String name) you probably don't want this function.
     * @param name the name to attempt to use.
     */
    @Override
    public void setNick(final String name) {
        super.setName(name);
    }

    /**
     * Set the settings object.
     * @param agentSettings the settings to set
     */
    public void setSettings(final AgentSettings agentSettings) {
        this.settings = agentSettings;
    }

    /**
     * Attempt to set the channel topic. Sends to active channel.
     * @param topic The body of the topic to set.
     */
    protected void setTopic(final String topic) {
        setTopic(activeChannel, topic);
    }

    /**
     * Initiate agent shutdown Disconnects the agent, sets shutting down flag.
     */
    public void shutdown() {
        if (isConnected() && shuttingDown == false)
        {
            shuttingDown = true;
        	disconnect();
        }
    }

    /** Request active topic. */
    protected void topic() {
        getSuppressTopic().remove(activeChannel);
        sendRawLine(String.format("TOPIC %s", activeChannel.getName()));
    }

    /**
     * Request information about a nick.
     * @param nick a command delimited list of nicks.
     */
    protected void whois(final String nick) {
        sendRawLine(String.format("WHOIS %s", nick));
    }

    /**
     * @return The hash set of channels to suppress user list in.
     */
    public HashSet<Channel> getSuppressNames() {
        return suppressNames;
    }

    /**
     * @return  the Hash set of channels to suppress topic messages in.
     */
    public HashSet<Channel> getSuppressTopic() {
        return suppressTopic;
    }
}
