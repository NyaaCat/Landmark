package cat.nyaa.landmark.command;

import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.utils.UiUtils;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandmarkCommand extends CommandReceiver {
    private final LandmarkPlugin plugin;

    public LandmarkCommand(LandmarkPlugin plugin, LandmarkI18n _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
    }

    public static void tryToActiveLandmark(String landmarkName, Player player) {
        var plugin = LandmarkPlugin.instance;
        if (plugin == null) return;
        var landMarkManager = plugin.getLandmarkManager();
        if (landMarkManager == null) return;
        var landmark = landMarkManager.getLandmark(landmarkName);
        if (landmark == null) {
            LandmarkI18n.send(player, "command.landmark_not_found", landmarkName);
            return;
        }
        boolean hasActivePermission = false;
        if (landmark.getAutoActive()) {
            LandmarkI18n.send(player, "command.landmark_already_activated", landmarkName);
            return;
        }
        if (landmark.getNearbyActive() && landMarkManager.isNearbyLandmark(player, landmark)) {
            hasActivePermission = true;
        }
        var playerDataManager = plugin.getPluginDataManager();
        if (playerDataManager == null) return;
        var playerId = player.getUniqueId();
        boolean finalHasActivePermission = hasActivePermission;
        playerDataManager.isPlayerLandmarkAvailable(playerId, landmarkName)
                .thenApply(
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
                .thenApply(
                        aBoolean -> {
                            if (aBoolean) {
                                playerDataManager.setPlayerLandmarkAvailable(playerId, landmarkName, true)
                                        .thenAccept((b) -> {
                                                    if (b) {
                                                        LandmarkI18n.sendPlayerSync(playerId, "command.activate.success", landmarkName);
                                                    } else {
                                                        LandmarkI18n.sendPlayerSync(playerId, "command.activate.fail", landmarkName);
                                                    }
                                                }
                                        );

                                return true;
                            }
                            return false;
                        }
                );
    }

    @SubCommand(value = "menu", isDefaultCommand = true, permission = "landmark.command.menu")
    public void menu(CommandSender sender, Arguments args) {
        if (LandmarkCommandManager.checkBusy(sender)) return;
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        UiUtils.openPlayerMenu(player);
    }

    @SubCommand(value = "teleport", alias = {"tp"}, permission = "landmark.command.teleport")
    public void teleport(CommandSender sender, Arguments args) {
        if (LandmarkCommandManager.checkBusy(sender)) return;
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        String landmarkName = args.nextString();

        final var playerId = player.getUniqueId();
        final var landmarkManager = plugin.getLandmarkManager();
        final var playerDataManager = plugin.getPluginDataManager();
        if (landmarkManager == null || playerDataManager == null) return;
        var landmark = landmarkManager.getLandmark(landmarkName);
        if (landmark == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", landmarkName);
            return;
        }

        if (landmark.getAutoActive()) {
            landmarkManager.teleportPlayerToLandmarkName(playerId, landmarkName);
            return;
        }

        playerDataManager.isPlayerLandmarkAvailable(playerId, landmarkName).thenAccept(aBoolean -> {
            if (aBoolean) {
                landmarkManager.teleportPlayerToLandmarkName(playerId, landmarkName);
            } else {
                LandmarkI18n.sendPlayerSync(playerId, "message.landmark.no_permission", landmarkName);
            }
        });
    }

    @SubCommand(value = "activate", permission = "landmark.command.activate")
    public void activate(CommandSender sender, Arguments args) {
        if (LandmarkCommandManager.checkBusy(sender)) return;
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
        tryToActiveLandmark(landmarkName, player);

    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
