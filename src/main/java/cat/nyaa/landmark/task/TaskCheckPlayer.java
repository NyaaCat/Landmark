package cat.nyaa.landmark.task;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.command.LandmarkCommand;
import cat.nyaa.landmark.config.LandmarkConfig;
import cat.nyaa.landmark.db.landmark.LandMarkManager;
import cat.nyaa.landmark.db.landmark.Landmark;
import cat.nyaa.landmark.db.playerData.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TaskCheckPlayer implements Runnable {
    private long tickNum;

    @Override
    public void run() {
        if(!PlayerDataManager.isIdle())return;
        this.tickNum++;
        if (LandmarkPlugin.instance == null) return;
        LandMarkManager landMarkManager = LandmarkPlugin.instance.getLandmarkManager();
        if (LandmarkPlugin.instance == null) return;
        LandmarkConfig landmarkConfig = LandmarkPlugin.instance.getLandmarkConfig();
        if (landMarkManager == null || landmarkConfig == null) return;
        final var nearbyDistance = landmarkConfig.nearbyDistance;

        var allLandmark = landMarkManager.getLandmarks();
        Bukkit.getOnlinePlayers().forEach((p) -> {
            TaskUtils.tickScheduler.mod32TickToRun(tickNum, p.getUniqueId(),
                    () -> playerTick(p, allLandmark, nearbyDistance)
            );
        });
    }

    private void playerTick(Player player, List<Landmark> allLandmark, double nearbyDistance) {
        if (LandmarkPlugin.instance == null) return;
        PlayerDataManager playerDataManager = LandmarkPlugin.instance.getPluginDataManager();
        if (playerDataManager == null) return;

        var playerId = player.getUniqueId();
        var nearbyLandmarks = allLandmark.stream().filter((l) -> filterNearbyLandMark(player, l, nearbyDistance)).toList();
        playerDataManager.getPlayerAvailableLandmarkNames(playerId).thenAcceptAsync((names) -> {
                    for (Landmark nearbyLandmark : nearbyLandmarks) {
                        if (!nearbyLandmark.getNearbyActive()) continue;
                        if (names.contains(nearbyLandmark.getName())) continue;
                        TaskUtils.async.callSyncAndGet(() -> {
                            var p = Bukkit.getPlayer(playerId);
                            if (p != null)
                                LandmarkCommand.tryToActiveLandmark(nearbyLandmark.getName(), p);
                            return null;
                        });

                    }
                }
        );
    }

    private boolean filterNearbyLandMark(Player player, Landmark landmark, double nearbyDistance) {
        var playerLocation = player.getLocation();
        if (!playerLocation.isWorldLoaded()) return false;
        var playerWorld = playerLocation.getWorld();
        if (playerWorld == null) return false;
        if (!playerWorld.getName().equals(landmark.getWorldName())) return false;
        if (playerLocation.getX() > (landmark.getX() + nearbyDistance)) return false;
        if (playerLocation.getY() > (landmark.getY() + nearbyDistance)) return false;
        if (playerLocation.getZ() > (landmark.getZ() + nearbyDistance)) return false;
        if (playerLocation.getX() < (landmark.getX() - nearbyDistance)) return false;
        if (playerLocation.getY() < (landmark.getY() - nearbyDistance)) return false;
        if (playerLocation.getZ() < (landmark.getZ() - nearbyDistance)) return false;
        return playerLocation.distanceSquared(landmark.getLocation()) < (nearbyDistance * nearbyDistance);
    }
}
