package hef.IRCTransport;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
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
    private HashSet<Channel> suppressNames;
    /**
     * A set of channels to suppress Topic message. This is used to hid initial
     * join messages.
     */
    private HashSet<Channel> suppressTopic;

    /**
     * Agent Constructor.
     * @param instance Reference to plugin instance.
     * @param bukkitPlayer Reference to Bukkit Player
     */
    public IrcAgent(final IRCTransport instance, final Player bukkitPlayer) {
        this.plugin = instance;
        this.player = bukkitPlayer;
        this.shuttingDown = false;
        setLogin(String.format("%s", player.getEntityId()));
        super.setAutoNickChange(true);

        setSuppressNames(new HashSet<Channel>());
        setSuppressTopic(new HashSet<Channel>());

        // init player settings
        setSettings(plugin.getDatabase().find(AgentSettings.class, player.getName()));
        if (null == getSettings()) {
            setSettings(new AgentSettings(player));
            String prefix = plugin.getConfig().getString("default.prefix", "");
            String suffix = plugin.getConfig().getString("default.suffix", "");
            getSettings().setIrcNick(String.format("%s%s%s", prefix, player.getName(), suffix));
        } else {
            LOG.log(Level.INFO, String.format("Player '%s' using persistent IRC nick '%s'", player.getName(), getSettings().getIrcNick()));
        }
        setNick(getSettings().getIrcNick());
        new Connect(this).run();
    }

    /**
     * Connect the agent. Don't call this directly, call `new
     * Connect(this).run()` instead.
     * @throws IOException If it was not possible to connect to the server.
     * @throws IrcException If the server would not let us join it.
     */
    public void connect() throws IOException, IrcException {
        if (!isConnected()) {
            if (getServer() == null) {
                connect(getPlugin().getConfig().getString("server.address"), getPlugin().getConfig().getInt("server.port"), getPlugin().getConfig().getString("server.password"));
            } else {
                reconnect();
            }
        }
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
        sendRawLine("NAMES " + activeChannel2);
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
        sendAction(activeChannel, action);
        getPlayer().sendMessage(String.format("[%s] * %s %s", activeChannel, getPlayer().getDisplayName(), action));
    }

    /**
     * Sends a message to the active channel.
     * @param message The message to send
     */
    public void sendMessage(final String message) {
        sendMessage(activeChannel, message);
        if (isConnected()) {
            String msg = String.format("[%s] %s: %s", activeChannel, getPlayer().getDisplayName(), message);
            getPlayer().sendMessage(msg);
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
        shuttingDown = true;
        disconnect();
    }

    /** Request active topic. */
    protected void topic() {
        getSuppressTopic().remove(activeChannel);
        sendRawLine(String.format("TOPIC %s", activeChannel));
    }

    /**
     * Request information about a nick.
     * @param nick a command delimited list of nicks.
     */
    protected void whois(final String nick) {
        sendRawLine(String.format("WHOIS %s", nick));
    }

    public HashSet<Channel> getSuppressNames() {
        return suppressNames;
    }

    public void setSuppressNames(HashSet<Channel> hashSet) {
        this.suppressNames = hashSet;
    }

    public HashSet<Channel> getSuppressTopic() {
        return suppressTopic;
    }

    public void setSuppressTopic(HashSet<Channel> suppressTopic) {
        this.suppressTopic = suppressTopic;
    }
}
