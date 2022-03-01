package cat.nyaa.landmark.utils;

import cat.nyaa.landmark.LandmarkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

public class ThreadUtils {
    public static <T> T callSyncAndGet(@NotNull Callable<T> callable) {
        return callSyncAndGet(callable, null);
    }

    @Nullable
    public static <T> T callSyncAndGet(@NotNull Callable<T> callable, @Nullable Plugin plugin) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            var p = plugin;
            if (p == null) p = LandmarkPlugin.instance;
            if (p == null) return null;
            try {
                return Bukkit.getScheduler().callSyncMethod(p, callable).get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
