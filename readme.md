[CHAT] IRCTransport 0.1 - Minecraft Chat/IRC integration
=============================================================

Replaces minecraft's in game chat with IRC clients.
This is a bukkit plugin.  
You must set irc.server=irc.example.com in server.properties first!

Available settings:
------------------
put these in server.properties
    irc.server=""
    irc.autojoin=""
Available commands:
-------------------
    /join #channel
    /leave #channel
    /channel #channel -- changes active channel
    /msg user -- send a private message to a user
    /nick new_name  -- changes chat name


[Download](https://github.com/downloads/hef/IRCTransport/IRCTransport-v0.2.jar)  
[Source](https://github.com/hef/IRCTransport)

Features:
---------
  * Minecraft chat replaced with IRC chat
  * private messaging 
  * IRC Channel are reflected in game.

Changelog:
----------
### Version 0.2
  * players name displays correctly when there nick is changed
  * nick change notification
  * nick already in use handling changed.
  * active channel is switched on channel join
  * Channel join messages
  * Channel autojoin now a setting.

### Version 0.1
  * Basic irc features are functional in Minecraft.
