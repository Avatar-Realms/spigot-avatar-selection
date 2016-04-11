package net.avatar.realms.spigot.avatar.selection;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class PlayerStatSerializer implements JsonSerializer<PlayerStat>, JsonDeserializer<PlayerStat>{
    @Override
    public PlayerStat deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Player player = null;
        JsonObject root = (JsonObject) jsonElement;
        String str = root.get("uuid").getAsString();
        UUID id = UUID.fromString(str);
        player = Bukkit.getPlayer(id);
        PlayerStat stat = new PlayerStat(player);
        short pres = root.get("presence").getAsShort();
        stat.setPresence(pres, true);
        return stat;
    }

    @Override
    public JsonElement serialize(PlayerStat playerStat, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject root = new JsonObject();

        root.add("uuid", new JsonPrimitive(playerStat.getPlayer().getUniqueId().toString()));
        root.add("name", new JsonPrimitive(playerStat.getPlayer().getName()));
        root.add("presence", new JsonPrimitive(playerStat.getPresence()));

        return root;
    }
}
