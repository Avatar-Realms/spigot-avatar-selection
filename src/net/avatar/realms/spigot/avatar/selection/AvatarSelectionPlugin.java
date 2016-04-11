package net.avatar.realms.spigot.avatar.selection;


import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class AvatarSelectionPlugin extends JavaPlugin implements Listener {

    private static AvatarSelectionPlugin plugin;

    private AvatarsData avatars;
    private Map<UUID, PlayerStat> stats;
    private Saver saver;

    @Override
    public void onEnable() {
        plugin = this;
        stats = new HashMap<UUID, PlayerStat>();
        this.saver = new Saver(this);
        avatars = saver.loadAvatarsData();
        for (PlayerStat stat : saver.loadAllStats()) {
            stats.put(stat.getPlayer().getUniqueId(), stat);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        //TODO count week
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argArray) {

        LinkedList<String> args = new LinkedList<String>(Arrays.asList(argArray));
        if (args.isEmpty()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must send parameters with this command.");
            return true;
        }
        String subCommand = args.remove(0).toUpperCase();
        if (subCommand.equals("LIST")) {
            listAvatars(sender);
        } else if (subCommand.equals("ADD")) {
            addAvatar(sender, args);
        } else if (subCommand.equals("SET")) {
            setAvatar(sender, args);
        } else if (subCommand.equals("NEW")) {
            randomNewAvatar(sender, args);
        } else {
            printUsage(sender);
        }
        return true;
    }

    private void randomNewAvatar(CommandSender sender, LinkedList<String> args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must be an operator to execute that command.");
            return;
        }
        if (args.size() != 1) {
            sender.sendMessage(ChatColor.DARK_RED + "Invalid parameter amount.");
            return;
        }

        // Get and check the element that the operator has entered
        BendingElement element = BendingElement.getType(args.remove(0));
        if ((element == null) || (element == BendingElement.NONE)) {
            sender.sendMessage(ChatColor.DARK_RED + "Invalid element name.");
            return;
        }
        List<Player> players = new ArrayList<Player>();

        // Add all the players with that element to a list
        for (Player player : Bukkit.getOnlinePlayers()) {
            BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
            if (bender == null) {
                continue;
            }
            if (bender.isBender(element)) {
                players.add(player);
            }
        }

        // Remove all previous avatar from the list
        for (Avatar avatar : this.avatars.avatars.values()) {
            if (players.contains(avatar.getPlayer())) {
                players.remove(avatar.getPlayer());
            }
        }

        if (players.isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("Hmm... this is bad... there seems to be no valid players for the avatar election.");
                }
            }
            return;
        }

        // Generate a random number between 0 and the amount of valid connected
        // players
        Random rand = new Random();
        int i = rand.nextInt(players.size());

        // Get the player matching this random number
        Player congrats = players.get(i);

        // Tell it first to staff
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage("New Avatar : " + congrats.getName());
            }
        }

        //Tell it to everyone
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*12, 0));
        }
        NewAvatarDisplayTask display = new NewAvatarDisplayTask(plugin, congrats, players);
        display.runTaskLaterAsynchronously(plugin, 20);
    }

    @SuppressWarnings("deprecation")
    private void setAvatar(CommandSender sender, LinkedList<String> args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must be oped to execute that command.");
            return;
        }
        try {
            if (args.size() != 3) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid parameter amount.");
                return;
            }
            int number = Integer.parseInt(args.remove(0));
            if (number < 1) {
                sender.sendMessage(ChatColor.DARK_RED + "You must enter a POSITIVE number as first parameter.");
                return;
            }
            String playerName = args.remove(0);
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid player name.");
                return;
            }

            String elementName = args.remove(0);
            BendingElement element = BendingElement.getType(elementName);
            if (element == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid element.");
                return;
            }

            Avatar avatar = new Avatar();
            avatar.setElement(element);
            avatar.setPlayer(player);
            avatar.setPosition(number);

            this.avatars.avatars.put(number, avatar);
            this.avatars.currentAvatar = avatar;
            saver.saveAvatarsData(avatars);
            sender.sendMessage(playerName + " is the " + element.name() + " avatar #" + number);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.DARK_RED + "You must enter a number as first parameter.");
        }
    }

    @SuppressWarnings("deprecation")
    private void addAvatar(CommandSender sender, LinkedList<String> args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must be oped to execute that command.");
            return;
        }
        if (args.size() != 3) {
            sender.sendMessage(ChatColor.DARK_RED + "Invalid parameter amount.");
            return;
        }
        try {
            int number = Integer.parseInt(args.remove(0));
            if (number < 1) {
                sender.sendMessage(ChatColor.DARK_RED + "You must enter a POSITIVE number as first parameter.");
                return;
            }
            String playerName = args.remove(0);
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid player name.");
                return;
            }

            String elementName = args.remove(0);
            BendingElement element = BendingElement.getType(elementName);
            if (element == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid element.");
                return;
            }

            Avatar avatar = new Avatar();
            avatar.setElement(element);
            avatar.setPlayer(player);
            avatar.setPosition(number);

            this.avatars.avatars.put(number, avatar);
            sender.sendMessage(playerName + " is the " + element.name() + " avatar #" + number);
            saver.saveAvatarsData(avatars);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.DARK_RED + "You must enter a number as first parameter.");
        }
    }

    private void printUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "/avatar list");
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.GOLD + "--Add a previous avatar : ");
            sender.sendMessage(ChatColor.GOLD + "/avatar add <avatarnumber> <playername> <avatarelement>");
            sender.sendMessage(ChatColor.GOLD + "--Set the current avatar : ");
            sender.sendMessage(ChatColor.GOLD + "/avatar set <avatarnumber> <playername> <avatarelement>");
            sender.sendMessage(ChatColor.GOLD + "--Select a new avatar : ");
            sender.sendMessage(ChatColor.GOLD + "/avatar new <avatarelement>");
        }
    }

    private void listAvatars(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "You must be a player to execute that command.");
            return;
        }

        if (this.avatars.avatars.isEmpty()) {
            sender.sendMessage(ChatColor.DARK_RED + "There is no avatar.");
            return;
        }
        Avatar first = this.avatars.avatars.get(1);
        StringBuilder string = new StringBuilder();
        string.append(PluginTools.getColor(Settings.getColor(first.getElement())));
        string.append(first.getPlayer().getName());
        for (Avatar av : this.avatars.avatars.values()) {
            if (av == first) {
                continue;
            }
            string.append(",");
            string.append(PluginTools.getColor(Settings.getColor(av.getElement())));
            string.append(av.getPlayer().getName());
        }
        sender.sendMessage(string.toString());
    }


}
