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
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 * Represent a player to an IRC server. Every Bukkit player should have one!
 * Every agent will have channels, a player, and an active channel.
 */
public class IrcAgent extends PircBot {

    /** Used to send message to the console. */
    private static final Logger LOG = Logger.getLogger("Minecraft");
    /** The active channel. */
    private String activeChannel;
    /** A reference to the Bukkit Player object. */
    private Player player;
    /** A reference to the IRCTransport plugin instance. */
    private final IRCTransport plugin;
    /** The settings object associated with this agent. */
    private AgentSettings settings;
    /** Flag to indicate we should not reconnect. */
    private boolean shuttingDown;
    /** A set of channels to suppress onUserList.  This is used to hide initial join messages. */
    private HashSet<String> suppressNames;
    /** A set of channels to suppress Topic message.  This is used to hid initial join messages. */
    private HashSet<String> suppressTopic;

    /**
     * Agent Constructor.
     *
     * @param instance
     *            Reference to plugin instance.
     * @param bukkitPlayer
     *            Reference to Bukkit Player
     */
    public IrcAgent(final IRCTransport instance, final Player bukkitPlayer) {
        this.plugin = instance;
        this.player = bukkitPlayer;
        this.shuttingDown = false;
		setLogin(String.format("%s", player.getEntityId()));
        super.setAutoNickChange(true);

        suppressNames = new HashSet<String>();
        suppressTopic = new HashSet<String>();

        // init player settings
        setSettings(plugin.getDatabase().find(AgentSettings.class,
                player.getName()));
        if (null == getSettings()) {
            setSettings(new AgentSettings(player));
            String prefix = plugin.getConfig().getString("default.prefix", "");
            String suffix = plugin.getConfig().getString("default.suffix", "");
            getSettings().setIrcNick(
                    String.format("%s%s%s", prefix, player.getName(), suffix));
        } else {
            LOG.log(Level.INFO, String.format(
                    "Player '%s' using persistent IRC nick '%s'",
                    player.getName(), getSettings().getIrcNick()));
        }
        setNick(getSettings().getIrcNick());
        new Connect(this).run();
    }

    /** Connect the agent.
     * Don't call this directly, call `new Connect(this).run()` instead.
     * @throws IOException If it was not possible to connect to the server.
     * @throws IrcException If the server would not let us join it.
     */
    public void connect() throws IOException, IrcException {
        if (getServer() == null) {
            connect(getPlugin().getConfig().getString("server.address"),
                    getPlugin().getConfig().getInt("server.port"),
                    getPlugin().getConfig().getString("server.password"));
        } else {
            reconnect();
        }
    }

    /**
     * Fetch the active channel. The active channel is the channel that a player
     * will talk in if they don't specify a channel.
     *
     * @return a string with the active channel name.
     */
    public String getActiveChannel() {
        return this.activeChannel;
    }

    /**
     * Get the Player.
     *
     * @return Reference to Bukkit Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The IRCTransport plugin instance.
     *
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
     *
     * @return Is the agent shutting down?
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Log stuff. This method only logs to INFO if the Verbose flags is set.
     *
     * @param line
     *            The line you want logged to console.
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
     * Get a list of playernames from a channel.
     *  removes muteNames flag for channel.
     * @param channel
     *            The channel to list names from.
     */
    protected void names(final String channel) {
        suppressNames.remove(channel);
        sendRawLine("NAMES " + channel);
    }

    /**
     * Handle receiving an action. sent when another agent sends a /me
     *
     * @param sender
     *            the person committing the action
     * @param login
     *            The login name of the actioner
     * @param hostname
     *            the hostname of the actioner
     * @param target
     *            The channel the action was in
     * @param action
     *            The content of the aciton
     */
    @Override
    public void onAction(final String sender, final String login,
            final String hostname, final String target, final String action) {
        getPlayer().sendMessage(
                String.format("[%s] * %s %s", target, sender, action));
    }

    /** Join Correct Channels.  Set name and topic suppression flags. */
    @Override
    public void onConnect() {
        /*
         * String webirc =
         * plugin.getConfig().getString("server.webirc_password"); if(webirc !=
         * null) { this.sendRawLine(String.format("WEBIRC %s %s %s %s", webirc,
         * player.getName(), player.getAddress().getHostName(),
         * player.getAddress().getAddress())); }
         */

        // The player may have not gotten then name they wanted.
        getPlayer().setDisplayName(getNick());
        getSettings().setIrcNick(getNick());

        boolean bSuppressNames = plugin.getConfig().getBoolean("suppress.initial_userlist", false);
        boolean bSuppressTopic = plugin.getConfig().getBoolean("suppress.initial_topic", false);
        List<?> channelData = plugin.getConfig().getList("default.channels");

        for (Object i : channelData) {
            if (i instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> linkedHashMapI = (LinkedHashMap<?, ?>) i;

                String channel = (String) linkedHashMapI.get("channel");
                if (channel != null) {
                    if (bSuppressNames) {
                        suppressNames.add(channel);
                    }
                    if (bSuppressTopic) {
                        suppressTopic.add(channel);
                    }
                    String key = (String) linkedHashMapI.get("key");
                    if (key != null) {
                        joinChannel(channel, key);
                    } else {
                        joinChannel(channel);
                    }
                }
            } else {
                log("Object: " + i.toString() + " is a "
                        + i.getClass().toString());
            }
        }
    }

    /**
     * Disconnect Handler. Will schedule a reconnect if not shutting down.
     */
    @Override
    public void onDisconnect() {
        getPlayer().sendMessage("ChatService Disconnected.");
        if (!shuttingDown) {
            new Connect(this).run();
        }
    }

    /**
     * Error message handler.
     *
     * @param channel
     *            The channel the error message was sent from.
     * @param message
     *            The error message body.
     */
    protected void onErrorMessage(final String channel, final String message) {
        getPlayer().sendMessage(
                ChatColor.YELLOW + String.format("[%s] %s", channel, message));
    }

    /**
     * Join message handler.
     *
     * @param channel
     *            the channel the player joined
     * @param sender
     *            the nick of the joiner
     * @param login
     *            the login of the joiner
     * @param hostname
     *            the hostname of the joiner
     */
    @Override
    public void onJoin(final String channel, final String sender,
            final String login, final String hostname) {
        // if I joined, change active channel.
        if (sender.equals(getNick())) {
            activeChannel = channel;
        }
        getPlayer().sendMessage(
                ChatColor.YELLOW
                + String.format("[%s] %s has joined.", channel,
                sender));
    }

    /**
     * Kick message handler.
     *
     * @param channel
     *            the channel the nick was kicked form
     * @param kickerNick
     *            the Nick of the Kicker
     * @param kickerLogin
     *            The login of the Kicker
     * @param kickerHostname
     *            the hostname of the kicker.
     * @param recipientNick
     *            The nick of the kickee.
     * @param reason
     *            The stated reason that the Kicker kicked the kickee.
     */
    @Override
    protected void onKick(final String channel, final String kickerNick,
            final String kickerLogin, final String kickerHostname,
            final String recipientNick, final String reason) {
        player.sendMessage(ChatColor.YELLOW
                + String.format("[%s] %s kicked by %s: %s", channel,
                recipientNick, kickerNick, reason));
    }

    /**
     * Message received handler.
     *
     * @param channel
     *            The channel of the message
     * @param sender
     *            The nick of the sender of the Message
     * @param login
     *            The login of the sender of the Message
     * @param hostname
     *            The hostname of the sender of the message
     * @param message
     *            The body of the message.
     */
    @Override
    public void onMessage(final String channel, final String sender,
            final String login, final String hostname, final String message) {
        getPlayer().sendMessage(
                String.format("[%s] %s: %s", channel, sender,
                ColorMap.fromIrc(message)));
    }

    /**
     * Nickchange notification handler. This is called when: a: Another agent
     * changes their nick b: when this agent change it's nick. Both these
     * situations are handled.
     *
     * @param oldNick
     *            the old nick of the changer.
     * @param login
     *            the login of the changer (doesn't change)
     * @param hostname
     *            the hostname of the changer (doesn't change)
     * @param newNick
     *            the new nick of the changer.
     */
    @Override
    protected void onNickChange(final String oldNick, final String login,
            final String hostname, final String newNick) {
        if (oldNick.equals(getPlayer().getDisplayName())) {
            getPlayer().setDisplayName(newNick);
            getSettings().setIrcNick(newNick);
            saveSettings();
        }
        getPlayer().sendMessage(
                String.format("%s is now known as %s", oldNick, newNick));
    }

    /**
     * The channel leave message handler.
     *
     * @param channel
     *            The channel that is being left
     * @param sender
     *            The nick of the leaver
     * @param login
     *            The login of the leaver
     * @param hostname
     *            The hostname of the leaver.
     */
    @Override
    public void onPart(final String channel, final String sender,
            final String login, final String hostname) {
        getPlayer().sendMessage(
                ChatColor.YELLOW
                + String.format("[%s] %s has parted.", channel,
                sender));
    }

    /**
     * Private message handler (/msg).
     *
     * @param sender
     *            nick of the private message sender
     * @param login
     *            login of the private message sender
     * @param hostname
     *            hostname of the private message sender
     * @param message
     *            the body of the private message
     */
    @Override
    public void onPrivateMessage(final String sender, final String login,
            final String hostname, final String message) {
        getPlayer().sendMessage(String.format("%s: %s", sender, message));
    }

    /**
     * Quit message handler. This is a quit message, it doesn't mean that we are
     * quitting.
     *
     * @param sourceNick
     *            Nick of the quitter.
     * @param sourceLogin
     *            Login of the quitter.
     * @param sourceHostname
     *            Hostname of the quitter.
     * @param reason
     *            The reason the quitter gave for quitting.
     */
    @Override
    public void onQuit(final String sourceNick, final String sourceLogin,
            final String sourceHostname, final String reason) {
        getPlayer().sendMessage(
                ChatColor.YELLOW
                + String.format("%s has quit: %s", sourceNick, reason));
    }

    /**
     * Handles response codes not handled by pircbot This methods handles irc
     * response codes, slices up the response, and then calls the appropriate
     * method.
     *
     * @param code
     *            the irc response code.
     * @param response
     *            The message that came with the response
     */
    @Override
    protected void onServerResponse(final int code, final String response) {
        Pattern responsePattern = Pattern.compile("\\S* (\\S*) :(.*)");
        Matcher responseMatcher = responsePattern.matcher(response);
        responseMatcher.find();
        switch (code) {
            case ERR_NOSUCHNICK: // TODO this needs a clearer error message
            case ERR_NOSUCHCHANNEL:
            case ERR_NICKNAMEINUSE:
            case ERR_INVITEONLYCHAN:
            case ERR_BADCHANNELKEY:
                onErrorMessage(responseMatcher.group(1), responseMatcher.group(2));
                break;
            default:
                break;

        }
    }

    /**
     * Handles topic responses. Topic responses come in: a: as a response to a
     * topic request b: on channel join c: on topic change
     * Honors muteTopic flag for channel, will clear it as well.
     * @param channel
     *            The channel the topic is set for.
     * @param topic
     *            The body of the topic.
     * @param setBy
     *            The nick of the topic setter.
     * @param date
     *            The date the topic was set.
     * @param changed
     *            Is this a new topic?
     */
    @Override
    protected void onTopic(final String channel, final String topic,
            final String setBy, final long date, final boolean changed) {
        if (changed) {
            getPlayer().sendMessage(
                    ChatColor.YELLOW
                    + String.format("[%s] Topic changed: %s", channel,
                    topic));
        } else {
            if (!suppressTopic.contains(channel)) {
                getPlayer().sendMessage(
                        ChatColor.YELLOW
                        + String.format("[%s] Topic: %s", channel,
                        topic));
            } else {
                // Only silence the initial topic response.
                suppressTopic.remove(channel);
            }
        }
    }

    /**
     * UserList Response handler. usually a response to /names.
     * Also occurs on channel join.  This will check the muteNames set, and clear it.
     * @param channel
     *            The channel that the response is for
     * @param users
     *            The users in the channel.
     */
    @Override
    protected void onUserList(final String channel, final User[] users) {
        if (suppressNames.contains(channel)) {
            suppressNames.remove(channel);
        } else {
            StringBuilder usersString = new StringBuilder();
            for (User user : users) {
                usersString.append(user.toString());
                usersString.append(" ");
            }
            getPlayer().sendMessage(
                    String.format("%s members: %s", channel, usersString.toString()));
        }
    }

    /** Save agent settings to persistent data store. */
    protected void saveSettings() {
        plugin.getDatabase().save(getSettings());
    }

    /**
     * Action sender. triggers when player sends a /me
     *
     * @param action
     *            The content of the action.
     */
    public void sendAction(final String action) {
        sendAction(activeChannel, action);
        getPlayer().sendMessage(
                String.format("[%s] * %s %s", activeChannel, getPlayer().getDisplayName(), action));
    }

    /**
     * Sends a message to the active channel.
     *
     * @param message
     *            The message to send
     */
    public void sendMessage(final String message) {
        sendMessage(activeChannel, message);
        if (isConnected()) {
            String msg = String.format("[%s] %s: %s", activeChannel,
                    getPlayer().getDisplayName(), message);
            getPlayer().sendMessage(msg);
        }
    }

    /**
     * Change active channel.
     *
     * @param channel
     *            The channel to make the active one.
     */
    public void setActiveChannel(final String channel) {
        this.activeChannel = channel;
    }

    /**
     * Set name to attempt to use at login This function is not the same as
     * changeNick(String name) you probably don't want this function.
     *
     * @param name
     *            the name to attempt to use.
     */
    public void setNick(final String name) {
        super.setName(name);
    }

    /**
     * Set the settings object.
     *
     * @param agentSettings
     *            the settings to set
     */
    public void setSettings(final AgentSettings agentSettings) {
        this.settings = agentSettings;
    }

    /**
     * Attempt to set the channel topic. Sends to active channel.
     *
     * @param topic
     *            The body of the topic to set.
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
        suppressTopic.remove(activeChannel);
        sendRawLine(String.format("TOPIC %s", activeChannel));
    }

    /**
     * Request information about a nick.
     *
     * @param nick
     *            a command delimited list of nicks.
     */
    protected void whois(final String nick) {
        sendRawLine(String.format("WHOIS %s", nick));
    }
}
