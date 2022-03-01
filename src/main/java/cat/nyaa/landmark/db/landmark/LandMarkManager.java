package cat.nyaa.landmark.db.landmark;

import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.db.landmarkDbManager;
import cat.nyaa.landmark.utils.ThreadUtils;
import cat.nyaa.nyaacore.utils.TeleportUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LandMarkManager {
    final ConcurrentHashMap<String, Landmark> landmarkMap = new ConcurrentHashMap<>();
    public final landmarkDbManager dbManager;

    public LandMarkManager(landmarkDbManager dbManager) {
        this.dbManager = dbManager;
        refreshLandmarkCache();
    }

    /**
     * Thread safe.
     *
     * @param name   landmark name
     */
    public boolean teleportPlayerToLandmarkName(UUID playerId, String name) {
        Boolean result = ThreadUtils.callSyncAndGet(() -> teleportPlayerToLandmarkName0(playerId, name), null);
        if (result == null) return false;
        return result;
    }

    private boolean teleportPlayerToLandmarkName0(UUID playerId, String name) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null) return false;
        var landmark = getLandmark(name);
        if (landmark == null) {
            LandmarkI18n.send(player, "message.landmark.not_found", name);
            return false;
        }
        if (TeleportUtils.Teleport(player, landmark.getLocation())) {
            LandmarkI18n.send(player, "message.landmark.teleport", landmark.getName());
            return true;
        }
        return false;
    }

    @Nullable
    public Landmark getLandmark(String name) {
        if (landmarkMap.containsKey(name)) {
            return landmarkMap.get(name);
        }
        return null;
    }

    public Set<String> getLandmarkNames() {
        return landmarkMap.keySet();
    }

    public List<Landmark> getLandmarks() {
        return Lists.newArrayList(landmarkMap.values());
    }

    public void setLandMark(@NotNull Landmark landmark) {
        dbManager.insertOrUpdateLandmark(landmark.getModel());
        refreshLandmarkCache();
    }


    public void removeLandmark(String name) {
        dbManager.removeLandmark(name);
        refreshLandmarkCache();
    }

    public synchronized void refreshLandmarkCache() {
        landmarkMap.clear();
        dbManager.getAllLandmarkModel().stream().map(m -> Landmark.fromModel(m, this)).forEach(landmark -> landmarkMap.put(landmark.getName(), landmark));
    }

    public boolean isNearbyLandmark(Player player, Landmark landmark) {
        var plugin = LandmarkPlugin.instance;
        if (plugin == null) return false;
        var nearbyDistance = plugin.getLandmarkConfig() != null ? plugin.getLandmarkConfig().nearbyDistance : 0d;
        if (nearbyDistance <= 0) return false;
        try {
            if (landmark.getLocation().distanceSquared(player.getLocation()) < (nearbyDistance * nearbyDistance))
                return true;
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }
}
