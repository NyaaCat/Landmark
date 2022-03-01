package cat.nyaa.landmark.config;

import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class LandmarkDatabaseConfig extends FileConfigure {
    private final LandmarkPlugin plugin;

    @Serializable
    public String sqlite_file = "pluginDatabase.db";

    public LandmarkDatabaseConfig(LandmarkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "landmarkDatabaseConfig.yml";
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }
}
