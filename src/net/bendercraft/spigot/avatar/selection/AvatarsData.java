package net.bendercraft.spigot.avatar.selection;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class AvatarsData {

	public Map<Integer, Avatar> avatars;
	public Avatar currentAvatar;
	public LocalDate lastElection;

	public AvatarsData() {
		this.avatars = new TreeMap<Integer, Avatar>();
		this.currentAvatar = null;
		this.lastElection = null;
	}
}
