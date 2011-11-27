/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import java.util.LinkedHashMap;
import java.util.List;
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
public class IrcListener extends ListenerAdapter<IrcAgent>{
    IRCTransport plugin;
    Logger log;
    
    public IrcListener(IRCTransport plugin)
    {
        log = plugin.getServer().getLogger();
        this.plugin = plugin;
    }
    
    /**
     * Handle receiving an action. sent when another agent sends a /me
     * @param sender the person committing the action
     * @param login The login name of the actioner
     * @param hostname the hostname of the actioner
     * @param target The channel the action was in
     * @param action The content of the aciton
     */
    @Override
    public void onAction(ActionEvent<IrcAgent> event)//final String sender, final String login, final String hostname, final String target, final String action) {
    {
        event.getBot().getPlayer().sendMessage(String.format("[%s] * %s %s", event.getChannel().getName(), event.getUser(), event.getAction()));
    }

    /** Join Correct Channels. Set name and topic suppression flags. */
    @Override
    public void onConnect(ConnectEvent<IrcAgent> event) {
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
                log.warning("Object: " + i.toString() + " is a " + i.getClass().toString());
            }
        }
    }

    /**
     * Disconnect Handler. Will schedule a reconnect if not shutting down.
     */
    @Override
    public void onDisconnect(DisconnectEvent<IrcAgent> event) {
        event.getBot().getPlayer().sendMessage("ChatService Disconnected.");
        if (!event.getBot().isShuttingDown()) {
            new Connect(event.getBot()).run();
        }
    }

    /**
     * Join message handler.
     * @param channel the channel the player joined
     * @param sender the nick of the joiner
     * @param login the login of the joiner
     * @param hostname the hostname of the joiner
     */
    @Override
    public void onJoin(JoinEvent<IrcAgent> event)
    {
        // if I joined, change active channel.
        if( event.getUser().equals( event.getBot().getUserBot() ) )
        {
            event.getBot().setActiveChannel(event.getChannel());
        }
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s has joined.", event.getChannel().getName(), event.getUser()));
    }

    /**
     * Kick message handler.
     * @param channel the channel the nick was kicked form
     * @param kickerNick the Nick of the Kicker
     * @param kickerLogin The login of the Kicker
     * @param kickerHostname the hostname of the kicker.
     * @param recipientNick The nick of the kickee.
     * @param reason The stated reason that the Kicker kicked the kickee.
     */
    @Override
    public void onKick(KickEvent<IrcAgent> event)
    {
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s kicked by %s: %s", event.getChannel().getName(), event.getRecipient(), event.getSource(), event.getReason()));
    }

    /**
     * Message received handler.
     * @param channel The channel of the message
     * @param sender The nick of the sender of the Message
     * @param login The login of the sender of the Message
     * @param hostname The hostname of the sender of the message
     * @param message The body of the message.
     */
    @Override
    public void onMessage(MessageEvent<IrcAgent> event)
    {
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
     * @param oldNick the old nick of the changer.
     * @param login the login of the changer (doesn't change)
     * @param hostname the hostname of the changer (doesn't change)
     * @param newNick the new nick of the changer.
     */
    @Override
    public void onNickChange(NickChangeEvent<IrcAgent> event)
    {
        if(event.getUser().equals(event.getBot().getUserBot()))
        {
            event.getBot().getPlayer().setDisplayName(event.getNewNick());
            event.getBot().getSettings().setIrcNick(event.getNewNick());
            event.getBot().saveSettings();
        }
        event.getBot().getPlayer().sendMessage(String.format("%s is now known as %s", event.getOldNick(), event.getNewNick()));
    }

    /**
     * The channel leave message handler.
     * @param channel The channel that is being left
     * @param sender The nick of the leaver
     * @param login The login of the leaver
     * @param hostname The hostname of the leaver.
     */
    @Override
    public void onPart(PartEvent<IrcAgent> event)
    {
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s has parted.", event.getChannel().getName(), event.getUser()));
    }

    /**
     * Private message handler (/msg).
     * @param sender nick of the private message sender
     * @param login login of the private message sender
     * @param hostname hostname of the private message sender
     * @param message the body of the private message
     */
    @Override
    public void onPrivateMessage(PrivateMessageEvent<IrcAgent> event)
    {
        event.getBot().getPlayer().sendMessage(String.format("%s: %s", event.getUser(), event.getMessage()));
    }

    /**
     * Quit message handler. This is a quit message, it doesn't mean that we are
     * quitting.
     * @param sourceNick Nick of the quitter.
     * @param sourceLogin Login of the quitter.
     * @param sourceHostname Hostname of the quitter.
     * @param reason The reason the quitter gave for quitting.
     */
    @Override
    public void onQuit(QuitEvent<IrcAgent> event)
    {
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + String.format("%s has quit: %s", event.getUser(), event.getReason()));
    }

    /**
     * Handles response codes not handled by pircbot This methods handles irc
     * response codes, slices up the response, and then calls the appropriate
     * method.
     * @param code the irc response code.
     * @param response The message that came with the response
     */
    @Override
    public void onUnknown(UnknownEvent<IrcAgent> event) {
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
                onErrorMessage(event, responseMatcher.group(2), responseMatcher.group(3));
                break;
            default:
                break;

        }
    }
    
    /**
    * Error message handler.
    * @param channel The channel the error message was sent from.
    * @param message The error message body.
    */
    protected void onErrorMessage(Event<IrcAgent> event, final String channel, final String message) {
        event.getBot().getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s", channel, message));
    }

    /**
     * Handles topic responses. Topic responses come in: a: as a response to a
     * topic request b: on channel join c: on topic change Honors muteTopic flag
     * for channel, will clear it as well.
     * @param channel The channel the topic is set for.
     * @param topic The body of the topic.
     * @param setBy The nick of the topic setter.
     * @param date The date the topic was set.
     * @param changed Is this a new topic?
     */
    @Override
    public void onTopic(TopicEvent<IrcAgent> event)
    {
        String format;
        String channel = event.getChannel().getName();
        String topic = event.getChannel().getTopic();
        Player player = event.getBot().getPlayer();
        
        if(event.getBot().getSuppressTopic().contains(event.getChannel()))
        {
            event.getBot().getSuppressTopic().remove(event.getChannel());
        }
        else if (event.isChanged())
        {
            format = ChatColor.YELLOW + "[%s] Topic changed: %s";
            String formattedMessage = String.format(format, channel, topic);
            player.sendMessage(formattedMessage);
        }
        else
        {
            format = ChatColor.YELLOW + String.format("[%s] Topic: %s");
            String formattedMessage = String.format(format, channel, topic);
            player.sendMessage(formattedMessage);
        }
    }

    /**
     * UserList Response handler. usually a response to /names. Also occurs on
     * channel join. This will check the muteNames set, and clear it.
     * @param channel The channel that the response is for
     * @param users The users in the channel.
     */
    @Override
    public void onUserList(UserListEvent<IrcAgent> event) {
        if (event.getBot().getSuppressNames().contains(event.getChannel())) {
            event.getBot().getSuppressNames().remove(event.getChannel());
        } else {
            StringBuilder usersString = new StringBuilder();
            for (User user : event.getUsers()) {
                usersString.append(user.toString());
                usersString.append(" ");
            }
            event.getBot().getPlayer().sendMessage(String.format("%s members: %s", event.getChannel().getName(), usersString.toString()));
        }
    }
}
