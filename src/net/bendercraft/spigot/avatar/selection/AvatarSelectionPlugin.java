package net.bendercraft.spigot.avatar.selection;


import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class AvatarSelectionPlugin extends JavaPlugin implements Listener, EventExecutor {

    private static AvatarSelectionPlugin plugin;

    private AvatarsData avatars;
    private Map<UUID, PlayerStat> stats;
    private Saver saver;

    @Override
    public void onEnable() {
        plugin = this;
        stats = new HashMap<>();
        this.saver = new Saver(this);
        avatars = saver.loadAvatarsData();
        for (PlayerStat stat : saver.loadAllStats()) {
            if (stat.getPlayer() != null) {
                stats.put(stat.getPlayer().getUniqueId(), stat);
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerStat stat = null;
        boolean changed = false;
        if (stats.containsKey(player.getUniqueId())) {
            stat = stats.get(player.getUniqueId());
        }
        if (stat == null) {
            stat = new PlayerStat(player);
            stats.put(player.getUniqueId(), stat);
            changed = true;
        }

        short currentWeek = getCurrentWeek();
        if (!stat.isPresent(currentWeek)) {
            stat.setPresence(currentWeek, true);
            changed = true;
        }

        if (changed) {
            saver.saveStats(stat);
        }
    }

    private short getCurrentWeek() {
        if (avatars.lastElection != null) {
            LocalDate now = LocalDate.now();
            LocalDate start = avatars.lastElection.plusDays(7);// The week after the avatar election
            if (now.isBefore(start)) {
                return PlayerStat.FIRST_WEEK;
            }
            start = start.plusDays(7);
            if (now.isBefore(start)) {
                return PlayerStat.SECOND_WEEK;
            }
            start = start.plusDays(7);
            if (now.isBefore(start)) {
                return PlayerStat.THIRD_WEEK;
            }
        }
        return PlayerStat.FOURTH_WEEK;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argArray) {

        LinkedList<String> args = new LinkedList<>(Arrays.asList(argArray));
        if (args.isEmpty()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must send parameters with this command.");
            return true;
        }
        String subCommand = args.remove(0).toUpperCase();
        if (subCommand.equals("LIST")) {
            listAvatars(sender);
        }
        else if (subCommand.equals("ADD")) {
            addAvatar(sender, args);
        }
        else if (subCommand.equals("SET")) {
            setAvatar(sender, args);
        }
        else if (subCommand.equals("NEW")) {
            randomNewAvatar(sender, args);
        }
        else if (subCommand.equals("RESET")) {
            resetMonth(sender, args);
        }
        else {
            printUsage(sender);
        }
        return true;
    }

    private void resetMonth(CommandSender sender, LinkedList<String> args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.DARK_RED + "You must be an operator to execute that command.");
            return;
        }
        for (PlayerStat stat : stats.values()) {
            stat.resetMonth();
        }
        avatars.lastElection = LocalDate.now();
        saver.saveAvatarsData(avatars);
        sender.sendMessage(ChatColor.GREEN + "Month statistics reset");
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
        List<Player> displayedList = new LinkedList<>();
        List<Player> players = new ArrayList<>();

        // Add all the players with that element to a list
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!avatars.hasPlayerAlreadyBeenAvatar(player)) {
                BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
                if (bender == null) {
                    continue;
                }
                if (bender.isBender(element)) {
                    PlayerStat stat = this.stats.get(player.getUniqueId());
                    if (stat != null) {
                        displayedList.add(player);
                        for (short i = 0; i < stat.getPresenceFactor(); i++) {
                            players.add(player);
                        }
                    }
                }
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

        Collections.shuffle(displayedList);
        Collections.shuffle(players);
        // Generate a random number between 0 and the amount of valid connected
        // players
        Random rand = new Random();
        int i = rand.nextInt(players.size());

        // Get the player matching this random number
        Player congrats = players.get(i);
        String msg = "New Avatar : " + congrats.getName() + " (" + players.stream().filter((p) -> p == congrats).count() +"/" + players.size() + ")";

        // Tell it first to staff
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(msg);
            }
        }

        //Tell it to everyone
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*12, 0));
        }
        NewAvatarDisplayTask display = new NewAvatarDisplayTask(plugin, congrats, displayedList);
        display.runTaskLaterAsynchronously(plugin, 20);
        sender.sendMessage(ChatColor.GREEN + "Election done. You now should use" + ChatColor.GOLD + " /avatar reset " + ChatColor.GREEN + "to reset month statistics.");
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


    @Override
    public void execute(Listener listener, Event event) throws EventException {

    }
}
