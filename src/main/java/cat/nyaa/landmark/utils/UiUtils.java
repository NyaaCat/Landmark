package cat.nyaa.landmark.utils;

import cat.nyaa.aolib.aoui.PageUI;
import cat.nyaa.aolib.aoui.item.CommandUiItem;
import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.db.landmark.Landmark;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class UiUtils {
    public static void openAdminMenu(Player player) {
        var plugin = LandmarkPlugin.instance;
        if (plugin == null) return;
        var landmarkManager = plugin.getLandmarkManager();
        if (landmarkManager == null) return;
        openMenu(landmarkManager.getLandmarks(), player.getUniqueId(), true);
    }

    public static void openPlayerMenu(Player player) {
        var plugin = LandmarkPlugin.instance;
        if (plugin == null) return;
        var landmarkManager = plugin.getLandmarkManager();
        var playerDataManager = plugin.getPluginDataManager();
        if (landmarkManager == null || playerDataManager == null) return;
        playerDataManager.getPlayerAvailableLandmarkNames(player.getUniqueId()).thenAcceptAsync((names) -> ThreadUtils.callSyncAndGet(() -> {
            var landmarks = landmarkManager.getLandmarks().stream().filter(landmark -> names.contains(landmark.getName())).toList();
            openMenu(landmarks, player.getUniqueId(), false);
            return null;
        }));
    }

    private static void openMenu(@NotNull Collection<Landmark> landmark, UUID playerId, boolean isAdmin) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        var plugin = LandmarkPlugin.instance;
        if (plugin == null) return;
        var uiManager = plugin.getUiManager();
        if (uiManager == null) return;
        List<IUiItem> uiItemList = Lists.newArrayList(landmark.stream().map(l -> CommandUiItem.create(l.getIconItem(), uiManager, getMenuItemCommand(isAdmin, l), null)).filter(Objects::nonNull).toList());
        var ui = new PageUI(uiItemList, uiManager::broadcastChanges, getMenuTitle(isAdmin));
        uiManager.sendOpenWindow(player, ui);
    }

    private static String getMenuTitle(boolean isAdmin) {
        if (isAdmin) return LandmarkI18n.format("ui.adminmenu.title");
        return LandmarkI18n.format("ui.menu.title");
    }

    private static @NotNull String getMenuItemCommand(boolean isAdmin, Landmark landmark) {
        return isAdmin ? "landmarkadmin teleport " + landmark.getName() : "landmark teleport " + landmark.getName();
    }
}
