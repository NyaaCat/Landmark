package cat.nyaa.landmark.db.playerData;

import cat.nyaa.landmark.db.landmarkDbManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class PlayerDataManager {
    private static final LinkedBlockingQueue<Runnable> playerDataExecutorQueue = new LinkedBlockingQueue<>();
    public static final ExecutorService playerDataExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, playerDataExecutorQueue);
    private final landmarkDbManager dbManager;

    public PlayerDataManager(landmarkDbManager dbManager) {
        this.dbManager = dbManager;
    }

    public static boolean isBusy() {
        return playerDataExecutorQueue.size() >= 5;
    }

    public static boolean isIdle() {
        return playerDataExecutorQueue.size() <= 0;
    }

    @Contract("_ -> new")
    public static <U> @NotNull CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        if (playerDataExecutor.isShutdown()) throw new RuntimeException("playerDataExecutor is shutdown");
        return CompletableFuture.supplyAsync(supplier, playerDataExecutor);
    }

    public CompletableFuture<List<String>> getPlayerAvailableLandmarkNames(UUID playerId) {
        return supplyAsync(() -> dbManager.getPlayerData(playerId).stream().map(model -> model.landmarkName).toList());
    }

    public CompletableFuture<Optional<Integer>> getPlayerDataId(UUID playerId, String landmarkName) {
        return supplyAsync(() -> dbManager.getPlayerDataId(playerId, landmarkName));
    }

    public CompletableFuture<Boolean> isPlayerLandmarkAvailable(UUID playerId, String landmarkName) {
        return getPlayerDataId(playerId, landmarkName).thenApply(Optional::isPresent);
    }

    public CompletableFuture<Boolean> setPlayerLandmarkAvailable(UUID playerId, String landmarkName, boolean value) {
        return supplyAsync(() -> dbManager.setPlayerLandmarkAvailable(playerId, landmarkName, value));
    }
}
