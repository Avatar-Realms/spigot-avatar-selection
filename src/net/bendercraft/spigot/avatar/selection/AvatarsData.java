package net.bendercraft.spigot.avatar.selection;

import org.bukkit.OfflinePlayer;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;


public class AvatarsData
{
	public Map<Integer, Avatar> avatars;
	public Avatar currentAvatar;
	public LocalDate lastElection;

	public AvatarsData()
	{
		this.avatars = new TreeMap<>();
		this.currentAvatar = null;
		this.lastElection = null;
	}

	public boolean hasPlayerAlreadyBeenAvatar(OfflinePlayer player)
	{
		if (player == null)
		{
			return false;
		}
		for (Avatar avatar : avatars.values())
		{
			if (player.equals(avatar.getPlayer()))
			{
				return true;
			}
		}
		return false;
	}
}
