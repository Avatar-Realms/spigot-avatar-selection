package net.avatar.realms.spigot.avatar.selection;

import com.connorlinfoot.titleapi.TitleAPI;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Created by Nokorbis on 27/02/2016.
 */
public class NewAvatarDisplayTask extends BukkitRunnable {

    private AvatarSelectionPlugin plugin;
    private Player newAvatar;
    private List<Player> players;

    public NewAvatarDisplayTask(AvatarSelectionPlugin plugin, Player newAvatar, List<Player> players) {
        this.plugin = plugin;
        this.newAvatar = newAvatar;
        this.players = players;
    }
    @Override
    public void run() {
        // Tell it to everyone !
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GOLD + "[Avatar] " + ChatColor.RESET + "Raava se tourne vers : ");
        BendingPlayer bender = BendingPlayer.getBendingPlayer(players.get(0));
        ChatColor color = PluginTools.getColor(Settings.getColor(bender.getBendingTypes().get(0)));
        sb.append(color);
        for (Player player : players) {
            sb.append(player.getName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("...");
        Bukkit.getServer().broadcastMessage(sb.toString());

        plugin.getLogger().info("Avatar - " + sb.toString());
        plugin.getLogger().info("Avatar - Choosen one : " + newAvatar.getName());

        int size = players.size();
        int j = 0;
        String inspection = ChatColor.AQUA + "Raava inspecte...";
        for (int i = 0; i < 40; i++) {
            j = (j < size-1)? j+1 : 0;
            String potential = color + players.get(j).getName();
            for (Player player : Bukkit.getOnlinePlayers()) {
                TitleAPI.sendTitle(player, 0, 5, 3, inspection, potential);
            }
            try {
                Thread.sleep(250);
            }
            catch (InterruptedException e) {
            }
        }

        String winner = color + newAvatar.getName() + " !";
        String msg = ChatColor.GOLD + "Raava a choisi... " + winner;
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleAPI.sendTitle(player, 0, 100, 40, ChatColor.GOLD + "Raava a choisi...", winner);
            player.sendMessage(msg);
        }
    }
}
