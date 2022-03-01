package cat.nyaa.landmark.command;

import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.utils.UiUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class LandmarkCommand extends CommandReceiver {
    private final LandmarkPlugin plugin;

    public LandmarkCommand(LandmarkPlugin plugin, LandmarkI18n _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
    }

    @SubCommand(value = "menu", isDefaultCommand = true, permission = "landmark.command.menu")
    public void menu(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        UiUtils.openPlayerMenu(player);
    }

    @SubCommand(value = "teleport", alias = {"tp"}, permission = "landmark.command.teleport")
    public void teleport(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        String landmarkName = args.nextString();
        final var playerId = player.getUniqueId();
        final var landmarkManager = plugin.getLandmarkManager();
        final var playerDataManager = plugin.getPluginDataManager();
        if (landmarkManager == null || playerDataManager == null) return;
        playerDataManager.isPlayerLandmarkAvailable(playerId, landmarkName).thenAcceptAsync(aBoolean -> {
            if (aBoolean) {
                landmarkManager.teleportPlayerToLandmarkName(playerId, landmarkName);
            } else {
                LandmarkI18n.sendPlayerSync(playerId, "message.landmark.no_permission", landmarkName);
            }
        });
    }

    @SubCommand(value = "activate", permission = "landmark.command.activate")
    public void activate(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {

            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        String landmarkName = args.nextString();
        var landMarkManager = plugin.getLandmarkManager();
        if (landMarkManager == null) return;
        var landmark = landMarkManager.getLandmark(landmarkName);
        if (landmark == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", landmarkName);
            return;
        }
        boolean hasActivePermission = false;
        if (landmark.getAutoActive()) {
            hasActivePermission = true;
        }
        if (landmark.getNearbyActive() && landMarkManager.isNearbyLandmark(player, landmark)) {
            hasActivePermission = true;
        }
        var playerDataManager = plugin.getPluginDataManager();
        if (playerDataManager == null) return;
        var playerId = player.getUniqueId();
        boolean finalHasActivePermission = hasActivePermission;
        playerDataManager.isPlayerLandmarkAvailable(playerId, landmarkName)
                .thenApplyAsync(
                        (aBoolean) -> {
                            if (!aBoolean) {
                                if (!finalHasActivePermission) {
                                    LandmarkI18n.sendPlayerSync(playerId, "command.activate.no_permission", landmarkName);
                                    return false;
                                }
                                return true;
                            } else {
                                LandmarkI18n.sendPlayerSync(playerId, "command.activate.already_activated", landmarkName);
                                return false;
                            }
                        }
                )
                .thenAcceptAsync(
                        aBoolean -> {
                            if (aBoolean) {
                                try {
                                    playerDataManager.setPlayerLandmarkAvailable(playerId, landmarkName, true).get();
                                    LandmarkI18n.sendPlayerSync(playerId, "command.activate.success", landmarkName);
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
