[CHAT] IRCTransport 0.3 - Minecraft Chat/IRC Integration
=============================================================

Replaces minecraft's in game chat with IRC clients.
This is a bukkit plugin.  

Available settings:
------------------
Put these in Minecraft's server.properties file with appropriate values.
    irc.server=
    irc.autojoin=
    irc.verbose=false

irc.server is mandatory.  All other settings are optional.

Available commands:
-------------------
    /join #channel
    /leave #channel
    /channel #channel -- changes active channel
    /msg user -- send a private message to a user
    /nick new_name  -- change your display name.

[Download](https://github.com/downloads/hef/IRCTransport/IRCTransport-0.3.jar)  
[Source](https://github.com/hef/IRCTransport)

Features:
---------
  * Minecraft chat is replaced with an IRC session.
  * Private messaging works in game.
  * IRC channels are accessible in game.

Changelog:
----------
### Version 0.4
  * Changed output messages to use logging
  * Fixed bug in nickname changing.

### Version 0.3
  * renamed PlayerBot to IrcAgent
  * Fixed join message detection
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
