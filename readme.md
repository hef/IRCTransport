[CHAT] IRCTransport 0.11.1 - Minecraft Chat/IRC Integration
=============================================================

This [Bukkit](http://bukkit.org/) plugin replaces minecraft chat system with a connection to an IRC Server.  All IRC Connections happen Bukkit server side, so either run your own IRC server, or make sure that you are allowed to use mutiple connections on the IRC server of your choice.

Available settings:
-------------------
Put these in IRCTransport/plugin.yml file with appropriate values.


    verbose: false
    suppress:
      initial_userlist: false
      initial_topic: false
    server:
      address: localhost
      port: 6667
      password: server_password
    default:
      prefix: MC-
      suffix: -IRC
      channels:
        - channel: '#minecraft'
          key: channel_password
         - channel: '#help'

The server address  setting is mandatory.  All other settings are optional.

### Normal Settings
Your config.yml will look a lot like this:

    server:
      address: irc.example.com
    default:
      channels:
        - channel: '#minecraft' 

Available commands:
-------------------
    /join #channel [key]
    /leave #channel
    /channel #channel -- changes your active channel
    /msg user -- send a private message to a user
    /nick new_name  -- change your display name.
    /names -- shows users in your channel
    /me action to perform -- performs an irc action
    /topic -- get or set the channel topic
    /whois -- gets information about a nick

[Download](https://github.com/downloads/hef/IRCTransport/IRCTransport-0.11.1.jar)  
[Source](https://github.com/hef/IRCTransport)

Features:
---------
  * Minecraft chat is replaced with an IRC session.
  * Private messaging works in game.
  * IRC channels are joinable in game.

Changelog:
----------
## Version 0.11.1
  * fixed nullUSERNAMEnull bug

### Version 0.11
  * Fixed a potential reload bug.
  * Changed from using server.properties to using IRCTransport/config.yml
  * Added /whois support.
  * Added nick persistance.

### Version 0.10
  * Added nickname prefix and suffix options.
  * Fixed Automatic reconnect after plugin is disabled or server is stopped.
  * Added Error message for nick name already in use.
  * Fixed a null exception when the console tries to use irc commands.
  * Channel parts (leaving a channel) are now announced.
  * Channel kicks is now announced.
  * Added some handling for "Connection reset" errors.

### Version 0.9
  * Fixed PlayerJoinEvent/PlayerQuitEvent [changes](http://forums.bukkit.org/threads/oops-i-broke-your-plugins.599/#post-156352)
  * Added channel key support.
  * Added auto join key support.
  * Made system messages yellow.

### Version 0.8
  * Actually fixed bug that 0.7 was supposed to fix.
  * Added irc.password and irc.port configuration options. 

### Version 0.7
  * Fixed bug where IRC agent would reconnect after player disconnected.

### Version 0.6
  * Attempts to reconnect to IRC server on connection failure/disconnect.
  * Minecraft color to IRC color support in chat.
  * Color conversion code refactored.

### Version 0.5
  * Fixed a nickname change bug.
  * IRC color to minecraft color support in chat.
  * Removed [TSLPC](http://forums.bukkit.org/threads/oops-i-broke-your-plugins.599/#post-70677).
  * Channel topic support

### Version 0.4
  * Added /me support
  * Added /names support.
  * Added channel is invite only error message.
  * Changed output messages to use logging.
  * Fixed bug in nickname changing.

### Version 0.3
  * Renamed PlayerBot to IrcAgent.
  * Fixed join message detection.
  * Changed package name to hef.IRCTransport as per [request](http://forums.bukkit.org/threads/on-namespaces-please-do-not-use-bukkit-in-your-plugins.3732/).
  * Changed build system to Maven.

### Version 0.2
  * Player's name displays correctly when their name is changed.
  * Nick change notification added.
  * Nick already in use handling changed.
  * Active channel is switched on channel join.
  * Channel join messages.
  * Channel autojoin now a setting.

### Version 0.1
  * Basic irc features are functional in Minecraft.
