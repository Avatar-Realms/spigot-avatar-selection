package net.avatar.realms.spigot.avatar.selection;

import java.util.Map;
import java.util.TreeMap;

public class AvatarsData {

	public Map<Integer, Avatar> avatars;
	public Avatar currentAvatar;

	public AvatarsData() {
		this.avatars = new TreeMap<Integer, Avatar>();
		this.currentAvatar = null;
	}
}
