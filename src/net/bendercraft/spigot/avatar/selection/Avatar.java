package net.bendercraft.spigot.avatar.selection;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import org.bukkit.OfflinePlayer;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class Avatar {
    private OfflinePlayer player;
    private int position;
    private BendingElement element;

    public Avatar() {

    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    public void setPlayer(OfflinePlayer player) {
        this.player = player;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public BendingElement getElement() {
        return this.element;
    }

    public void setElement(BendingElement element) {
        this.element = element;
    }

}
