package net.bendercraft.spigot.avatar.selection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Nokorbis on 10/04/2016.
 */
public class Saver {

    private static final String CHARSET = "UTF-8";
    private static final String STATS_FOLDER = "Statistics";
    private static final String EXT = ".json";

    private AvatarSelectionPlugin plugin;
    private Gson gson;
    private File dataFolder;
    private FilenameFilter filter;

    public Saver(AvatarSelectionPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.filter = new JsonFilenameFilter();
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(PlayerStat.class, new PlayerStatSerializer());
        builder.registerTypeAdapter(AvatarsData.class, new AvatarSerializer());
        gson = builder.create();
    }

    public Collection<PlayerStat> loadAllStats() {
        if (dataFolder == null || !dataFolder.exists()) {
            return Collections.emptyList();
        }
        File folder = getStatsFolder();
        Collection<PlayerStat> stats = new LinkedList<PlayerStat>();

        for (File file : folder.listFiles(filter)) {
            PlayerStat playerStat = loadStats(file);
            if (playerStat != null) {
                stats.add(playerStat);
            }
        }

        return stats;
    }

    private PlayerStat loadStats(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            InputStream is = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(is, CHARSET);
            PlayerStat team = gson.fromJson(reader, PlayerStat.class);
            reader.close();
            return team;
        }
        catch (FileNotFoundException e) {
            plugin.getLogger().warning("Was not able to find a file while loading a statistics : " + file.getName());
        }
        catch (UnsupportedEncodingException e) {
            plugin.getLogger().warning("Was not able to handle the charset " + CHARSET);
        } catch (IOException e) {
            plugin.getLogger().warning("Was not able to read a file while loading a statistics");
        }
        return null;
    }

    public boolean saveStats(PlayerStat statistics) {
        try {
            File teamFolder = getStatsFolder();
            File file = new File(teamFolder, statistics.getPlayer().getUniqueId() + EXT);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, CHARSET);
            String json = gson.toJson(statistics);
            writer.write(json);
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            plugin.getLogger().severe("Was not able to find a file while saving a statistics : " + statistics.getPlayer().getUniqueId());
            return false;
        } catch (IOException e) {
            plugin.getLogger().severe("Was not able to create a file while saving a statistics : " + statistics.getPlayer().getUniqueId());
            return false;
        }
    }

    public void deleteStats(PlayerStat playerStat) {
        File toDelete = new File(getStatsFolder(), playerStat.getPlayer().getUniqueId() + EXT);
        if (toDelete.exists()) {
            toDelete.delete();
        }
    }

    public File getStatsFolder() {
        File teamFolder = new File(dataFolder, STATS_FOLDER);
        if (!teamFolder.exists()) {
            teamFolder.mkdirs();
        }
        return teamFolder;
    }

    public AvatarsData loadAvatarsData() {
        AvatarsData data;
        if (dataFolder.exists()) {
            File save = new File(dataFolder, "avatars.json");
            if (save.exists()) {
                FileReader reader = null;
                try {
                    reader = new FileReader(save);
                    data = this.gson.fromJson(reader, AvatarsData.class);
                    reader.close();
                } catch (IOException e) {
                    data = new AvatarsData();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                data = new AvatarsData();
            }
        } else {
            data = new AvatarsData();
        }
        return data;
    }

    public void saveAvatarsData(AvatarsData data) {
        FileWriter writer = null;
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File save = new File(dataFolder, "avatars.json");
            if (!save.exists()) {
                save.createNewFile();
            }
            writer = new FileWriter(save, false);
            this.gson.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class JsonFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(EXT);
        }
    }
}
