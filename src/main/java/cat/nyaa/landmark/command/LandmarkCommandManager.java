package cat.nyaa.landmark.command;

import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import org.jetbrains.annotations.NotNull;

public class LandmarkCommandManager {
    public final LandmarkCommand landmarkCommand;
    public final LandmarkAdminCommand landmarkAdminCommand;

    public LandmarkCommandManager(@NotNull LandmarkPlugin plugin, @NotNull LandmarkI18n i18n) {
        this.landmarkCommand = new LandmarkCommand(plugin, i18n);
        this.landmarkAdminCommand = new LandmarkAdminCommand(plugin, i18n);
        regCommand(plugin, "landmark", landmarkCommand);
        regCommand(plugin, "landmarkadmin", landmarkAdminCommand);
    }

    private void regCommand(@NotNull LandmarkPlugin plugin, @NotNull String commandName, @NotNull CommandReceiver commandReceiver) {
        var pluginCommand = plugin.getCommand(commandName);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Command registration failed : " + commandName + " not found.");
            return;
        }
        pluginCommand.setExecutor(commandReceiver);
        pluginCommand.setTabCompleter(commandReceiver);
    }

}
