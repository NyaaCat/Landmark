package cat.nyaa.landmark.config;

import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class LandmarkConfig extends PluginConfigure {
    private final LandmarkPlugin plugin;

    @StandaloneConfig
    public LandmarkDatabaseConfig landmarkDatabaseConfig;
    @Serializable
    public String language = LanguageRepository.DEFAULT_LANGUAGE;
    @Serializable(name = "max_activate_distance")
    public double nearbyDistance = 6.5;

    public LandmarkConfig(LandmarkPlugin plugin) {
        this.plugin = plugin;
        this.landmarkDatabaseConfig = new LandmarkDatabaseConfig(plugin);
        load();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;

    }
}
