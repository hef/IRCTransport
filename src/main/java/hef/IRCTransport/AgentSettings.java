package hef.IRCTransport;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/** Persistant Agent settings class.
 * This class utilizes bukkit ebean facilities.
 * Any Player/agent related information that should persist between sessions
 * is defined here.
 */
@Entity()
@Table(name = "players")
public class AgentSettings {
    /** The player's chosen name
     * This is the display name for the player
     * It is the value we want to retrieve.
     */
    @NotEmpty
    private String ircNick = null;

    /** The real player name.
     * This is the real player name not the nick.
     * This is also the primary key stored in the database.
     */
    @Id
    @NotNull
    private String playerName;

    /** Mandatory constructor
     * don't use this constructor, ebean needs it.
     */
    public AgentSettings() {
    }

    /** The main constructor.
     * builds on object based on the players realname.
     * It will gather the players nick from the database.
     * @param player the player's real name
     */
    public AgentSettings(final Player player) {
        setPlayerName(player.getName());
    }

    /** fetched stored nick
     * If the player has set the nick in the past, this field will retrieve
     * it from the database.
     * @return the nickname to fetch
     */
    public String getIrcNick() {
        return ircNick;
    }

    /** Fetch the stored player name from the database.
     * The rest of the data is matched based on this field.
     * @return the players name
     */
    public String getPlayerName() {
        return playerName;
    }

    /** Store off an irc nick.
     * Sets the nick to store in the database.
     * @param nick the new nick to use.
     */
    public void setIrcNick(final String nick) {
        this.ircNick = nick;
    }

    /**
     * If you don't have this, the persistence stuff gets cranky
     * This isn't used buy the IrcAgent class directly.
     * @param name The players real login name.
     */
    public void setPlayerName(final String name) {
        this.playerName = name;
    }
}
