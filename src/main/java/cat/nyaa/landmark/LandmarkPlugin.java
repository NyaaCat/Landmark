package cat.nyaa.landmark;

import cat.nyaa.aolib.aoui.UIManager;
import cat.nyaa.landmark.command.LandmarkCommandManager;
import cat.nyaa.landmark.config.LandmarkConfig;
import cat.nyaa.landmark.db.landmark.LandMarkManager;
import cat.nyaa.landmark.db.landmarkDbManager;
import cat.nyaa.landmark.db.playerData.PlayerDataManager;
import cat.nyaa.landmark.task.LandMarkTaskManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class LandmarkPlugin extends JavaPlugin {
    @Nullable
    private LandmarkConfig landmarkConfig;
    @Nullable
    private LandmarkI18n i18n;
    @Nullable
    private LandmarkCommandManager landmarkCommandManager;
    @Nullable
    private landmarkDbManager databaseManager;
    @Nullable
    private volatile LandMarkManager landmarkManager;
    @Nullable
    private PlayerDataManager pluginDataManager;
    @Nullable
    private UIManager uiManager;
    @Nullable
    public static LandmarkPlugin instance = null;
    @Nullable
    private LandMarkTaskManager taskManager = null;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.landmarkConfig = new LandmarkConfig(this);
        this.i18n = new LandmarkI18n(this, this.landmarkConfig.language);
        this.landmarkCommandManager = new LandmarkCommandManager(this, this.i18n);
        this.databaseManager = new landmarkDbManager(landmarkConfig.landmarkDatabaseConfig);
        this.landmarkManager = new LandMarkManager(databaseManager);
        this.pluginDataManager = new PlayerDataManager(databaseManager);
        this.uiManager = new UIManager(this);
        this.taskManager = new LandMarkTaskManager(this);
        // Plugin startup logic
    }

    public @Nullable UIManager getUiManager() {
        return uiManager;
    }

    public Optional<PlayerDataManager> getPluginDataManagerOpt() {
        return Optional.ofNullable(pluginDataManager);
    }

    @Nullable
    public synchronized LandMarkManager getLandmarkManager() {
        return landmarkManager;
    }

    public synchronized Optional<LandMarkManager> getLandmarkManagerOpt() {
        return Optional.ofNullable(landmarkManager);
    }

    public @Nullable LandmarkConfig getLandmarkConfig() {
        return landmarkConfig;
    }

    @Override
    public void onDisable() {
        if (uiManager != null) {
            uiManager.destructor();
            uiManager = null;
        }
        if (taskManager != null) {
            taskManager.destructor();
            taskManager = null;
        }
        // Plugin shutdown logic
    }

    @Nullable
    public PlayerDataManager getPluginDataManager() {
        return pluginDataManager;
    }

    public void onReload() {
        onDisable();
        onEnable();
    }
}
