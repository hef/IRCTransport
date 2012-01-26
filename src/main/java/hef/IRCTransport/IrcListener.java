/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.pircbotx.ReplyConstants;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.pircbotx.hooks.events.UserListEvent;

/**
 *
 * @author hef
 */
public class IrcListener extends ListenerAdapter<IrcAgent> {

    /** The owning plugin. */
    private IRCTransport plugin;
    /** The log object. */
    private Logger log;

    /**
     * Sets up object.
     * @param parentPlugin owning plugin.
     */
    public IrcListener(final IRCTransport parentPlugin) {
        this.plugin = parentPlugin;
        log = plugin.getServer().getLogger();

    }

    /**
     * Handle receiving an action. sent when another agent sends a /me
     * @param event Information derived from the event.
     */
    @Override
    public void onAction(final ActionEvent<IrcAgent> event) {
        String format = "[%s] * %s %s";
        String channel = event.getChannel().getName();
        String user = event.getUser().getNick();
        String action = event.getAction();
        String message = String.format(format, channel, user, action);
        event.getBot().getPlayer().sendMessage(message);
    }

    /** Join Correct Channels. Set name and topic suppression flags.
     * @param event Information derived from the event.
     */
    @Override
    public void onConnect(final ConnectEvent<IrcAgent> event) {
        /*
         * String webirc =
         * plugin.getConfig().getString("server.webirc_password"); if(webirc !=
         * null) { this.sendRawLine(String.format("WEBIRC %s %s %s %s", webirc,
         * player.getName(), player.getAddress().getHostName(),
         * player.getAddress().getAddress())); }
         */

        // The player may have not gotten then name they wanted.
        event.getBot().getPlayer().setDisplayName(event.getBot().getNick());
        event.getBot().getSettings().setIrcNick(event.getBot().getNick());

        boolean bSuppressNames = plugin.getConfig().getBoolean("suppress.initial_userlist", false);
        boolean bSuppressTopic = plugin.getConfig().getBoolean("suppress.initial_topic", false);
        List<?> channelData = plugin.getConfig().getList("default.channels");

        for (Object i : channelData) {
            if (i instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> linkedHashMapI = (LinkedHashMap<?, ?>) i;

                String channelName = (String) linkedHashMapI.get("channel");
                if (channelName != null) {
                    if (bSuppressNames) {
                        event.getBot().getSuppressNames().add(event.getBot().getChannel(channelName));
                    }
                    if (bSuppressTopic) {
                        event.getBot().getSuppressTopic().add(event.getBot().getChannel(channelName));
                    }
                    String key = (String) linkedHashMapI.get("key");
                    if (key != null) {
                        event.getBot().joinChannel(channelName, key);
                    } else {
                        event.getBot().joinChannel(channelName);
                    }
                }
            } else {
                log.log(Level.WARNING, "Object: {0} is a {1}", new Object[]{i.toString(), i.getClass().toString()});
            }
        }
    }

    /**
     * Disconnect Handler. Will schedule a reconnect if not shutting down.
     * @param event Information derived from the event.
     */
    @Override
    public void onDisconnect(final DisconnectEvent<IrcAgent> event) {
        event.getBot().getPlayer().sendMessage("ChatService Disconnected.");
        if (!event.getBot().isShuttingDown()) {
            new Connect(event.getBot()).run();
        }
    }

    /**
     * Join message handler.
     * @param event Information derived from the event.
     */
    @Override
    public void onJoin(final JoinEvent<IrcAgent> event) {
        // if I joined, change active channel.
        if (event.getUser().equals(event.getBot().getUserBot())) {
            event.getBot().setActiveChannel(event.getChannel());
        }
        String format = "[%s] %s has joined.";
        String channel = event.getChannel().getName();
        String user = event.getUser().getNick();
        String message = String.format(format, channel, user);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * Kick message handler.
     * @param event Information derived from the event.
     */
    @Override
    public void onKick(final KickEvent<IrcAgent> event) {
        String format = "[%s] %s kicked by %s: %s";
        String channel = event.getChannel().getName();
        String recipient = event.getRecipient().getNick();
        String source = event.getSource().getNick();
        String reason = event.getReason();
        String message = String.format(format, channel, recipient, source, reason);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * Message received handler.
     * @param event Information derived from the event.
     */
    @Override
    public void onMessage(final MessageEvent<IrcAgent> event) {
        String format = "[%s] %s: %s";
        String channel = event.getChannel().getName();
        String sender = event.getUser().getNick();
        String message = ColorMap.fromIrc(event.getMessage());
        String formattedMessage = String.format(format, channel, sender, message);
        event.getBot().getPlayer().sendMessage(formattedMessage);
        log.info(formattedMessage);
    }

    /**
     * Nickchange notification handler. This is called when: a: Another agent
     * changes their nick b: when this agent change it's nick. Both these
     * situations are handled.
     * @param event Information derived from the event.
     */
    @Override
    public void onNickChange(final NickChangeEvent<IrcAgent> event) {
        if (event.getUser().equals(event.getBot().getUserBot())) {
            event.getBot().getPlayer().setDisplayName(event.getNewNick());
            event.getBot().getSettings().setIrcNick(event.getNewNick());
            event.getBot().saveSettings();
        }
        String format = "%s is now known as %s";
        String oldNick = event.getOldNick();
        String newNick = event.getNewNick();
        String message = String.format(format, oldNick, newNick);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * The channel leave message handler.
     * @param event Information derived from the event.
     */
    @Override
    public void onPart(final PartEvent<IrcAgent> event) {
        String format = "[%s] %s has parted.";
        String channel = event.getChannel().getName();
        String user = event.getUser().getNick();
        String message = String.format(format, channel, user);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * Private message handler (/msg).
     * @param event Information derived from the event.
     */
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<IrcAgent> event) {
        String format = "%s: %s";
        String user = event.getUser().getNick();
        String text = event.getMessage();
        String message = String.format(format, user, text);
        event.getBot().getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + message);
    }

    /**
     * Quit message handler. This is a quit message, it doesn't mean that we are
     * quitting.
     * @param event Information derived from the event.
     */
    @Override
    public void onQuit(final QuitEvent<IrcAgent> event) {
        String format = "%s has quit: %s";
        String user = event.getUser().getNick();
        String reason = event.getReason();
        String message = String.format(format, user, reason);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * Handles response codes not handled by pircbot This methods handles irc
     * response codes, slices up the response, and then calls the appropriate
     * method.
     * @param event Information derived from the event.
     */
    @Override
    public void onUnknown(final UnknownEvent<IrcAgent> event) {
        Pattern responsePattern = Pattern.compile("(\\S*) (\\S*) :(.*)");
        Matcher responseMatcher = responsePattern.matcher(event.getLine());
        responseMatcher.find();
        int code = Integer.parseInt(responseMatcher.group(1));
        switch (code) {
            case ReplyConstants.ERR_NOSUCHNICK: // TODO this needs a clearer error message
            case ReplyConstants.ERR_NOSUCHCHANNEL:
            case ReplyConstants.ERR_NICKNAMEINUSE:
            case ReplyConstants.ERR_INVITEONLYCHAN:
            case ReplyConstants.ERR_BADCHANNELKEY:
                onErrorMessage(event, responseMatcher.group(2), responseMatcher.group(3));  //NOPMD
                break;
            default:
                break;

        }
    }

    /**
     * Error message handler.
     * @param event Information derived from the event.
     * @param channel The channel that originated the message.
     * @param message The error message.
     */
    protected void onErrorMessage(final Event<IrcAgent> event, final String channel, final String errorMessage) {
        String format = "[%s] %s";
        String message = String.format(format, channel, errorMessage);
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
    }

    /**
     * Handles topic responses. Topic responses come in: a: as a response to a
     * topic request b: on channel join c: on topic change Honors muteTopic flag
     * for channel, will clear it as well.
     * @param event Information derived from the event.
     */
    @Override
    public void onTopic(final TopicEvent<IrcAgent> event) {
        String format;
        String channel = event.getChannel().getName();
        String topic = event.getChannel().getTopic();
        Player player = event.getBot().getPlayer();

        if (event.getBot().getSuppressTopic().contains(event.getChannel())) {
            event.getBot().getSuppressTopic().remove(event.getChannel());
        } else if (event.isChanged()) {
            format = ChatColor.YELLOW + "[%s] Topic changed: %s";
            String formattedMessage = String.format(format, channel, topic);
            player.sendMessage(formattedMessage);
        } else {
            format = ChatColor.YELLOW + "[%s] Topic: %s";
            String formattedMessage = String.format(format, channel, topic);
            player.sendMessage(formattedMessage);
        }
    }

    /**
     * UserList Response handler. usually a response to /names. Also occurs on
     * channel join. This will check the muteNames set, and clear it.
     * @param event Information derived from the event.
     */
    @Override
    public void onUserList(final UserListEvent<IrcAgent> event) {
        if (event.getBot().getSuppressNames().contains(event.getChannel())) {
            event.getBot().getSuppressNames().remove(event.getChannel());
        } else {
            StringBuilder usersString = new StringBuilder();
            for (User user : event.getUsers()) {
                usersString.append(user.toString());
                usersString.append(" ");
            }
            String format = "%s members: %s";
            String channel = event.getChannel().getName();
            String message = String.format(format, channel, usersString.toString());
            event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + message);
        }
    }
}
