package net.avatar.realms.spigot.avatar.selection;

import com.google.gson.*;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.UUID;

public class AvatarSerializer implements JsonSerializer<AvatarsData>, JsonDeserializer<AvatarsData> {

	@Override
	public AvatarsData deserialize(JsonElement json, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		AvatarsData data = new AvatarsData();
		if (json == null || json.isJsonNull()) {
			return data;
		}
		JsonArray root = json.getAsJsonArray();
		JsonArray avatars = root.get(0).getAsJsonArray();
		Iterator<JsonElement> it = avatars.iterator();
		while (it.hasNext()) {
			JsonArray av = it.next().getAsJsonArray();
			int position = av.get(0).getAsInt();
			String uuid = av.get(1).getAsString();
			BendingElement element = BendingElement.getType(av.get(2).getAsString());
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
			if (p == null) {
				System.err.println("--------------------Null offline player was getting when loading avatars");
			}
			Avatar avatar = new Avatar();
			avatar.setPosition(position);
			avatar.setPlayer(p);
			avatar.setElement(element);
			data.avatars.put(position, avatar);
		}
		int currentPosition = root.get(1).getAsInt();
		data.currentAvatar = data.avatars.get(currentPosition);
		if (root.size() > 2) {
			JsonElement tmp = root.get(2);
			if (tmp != null) {
				data.lastElection = LocalDate.parse(tmp.getAsString());
			}
		}


		return data;
	}

	@Override
	public JsonElement serialize(AvatarsData data, Type arg1, JsonSerializationContext arg2) {
		JsonArray root = new JsonArray();
		JsonArray avatars = new JsonArray();
		for (Avatar a : data.avatars.values()) {
			JsonArray avatar = new JsonArray();
			avatar.add(new JsonPrimitive(a.getPosition()));
			avatar.add(new JsonPrimitive(a.getPlayer().getUniqueId().toString()));
			avatar.add(new JsonPrimitive(a.getElement().name()));
			avatars.add(avatar);
		}
		root.add(avatars);
		JsonPrimitive current = null;
		if (data.currentAvatar == null) {
			current = new JsonPrimitive(0);
		} else {
			current = new JsonPrimitive(data.currentAvatar.getPosition());
		}
		root.add(current);

		JsonPrimitive last = new JsonPrimitive(data.lastElection.toString());
		root.add(last);
		return root;
	}

}
