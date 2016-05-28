package net.bendercraft.spigot.avatar.selection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class PlayerStat {

    public static final short FIRST_WEEK  = 1;
    public static final short SECOND_WEEK = 2;
    public static final short THIRD_WEEK  = 4;
    public static final short FOURTH_WEEK = 8;

    /**
     *  Reference to the player whose belong these stats
     */
    private OfflinePlayer player;

    /**
     *  Connexions of the player during this month
     */
    private short presence;

    public PlayerStat() {
        this.presence = 0;
    }

    public PlayerStat(OfflinePlayer player) {
        this();
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public void resetMonth() {
        presence = 0;
    }

    public void setPresence(short week, boolean present) {
        if (present) {
            presence |= week;
        }
        else {
            presence &= ~week;
        }
    }

    public short getPresenceFactor() {
        short cpt = 0;
        if (isPresent(FIRST_WEEK)) {
            cpt++;
        }
        if (isPresent(SECOND_WEEK)) {
            cpt++;
        }
        if (isPresent(THIRD_WEEK)) {
            cpt++;
        }
        if (isPresent(FOURTH_WEEK)) {
            cpt++;
        }
        return cpt;
    }

    public boolean isPresent(short week) {
        return (presence & week) == week;
    }

    public short getPresence() {
        return presence;
    }

}
