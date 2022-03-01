package cat.nyaa.landmark.db.playerData;

import cat.nyaa.landmark.db.landmarkDbManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PlayerDataManager {
    private final landmarkDbManager dbManager;
    public static final ExecutorService playerDataExecutor = Executors.newSingleThreadExecutor();

    public PlayerDataManager(landmarkDbManager dbManager) {
        this.dbManager = dbManager;
    }

    public CompletableFuture<List<String>> getPlayerAvailableLandmarkNames(UUID playerId) {
        return supplyAsync(() -> dbManager.getPlayerData(playerId).stream().map(model -> model.landmarkName).toList());
    }

    public CompletableFuture<Optional<Integer>> getPlayerDataId(UUID playerId, String landmarkName) {
        return supplyAsync(() -> dbManager.getPlayerDataId(playerId, landmarkName));
    }

    public CompletableFuture<Boolean> isPlayerLandmarkAvailable(UUID playerId, String landmarkName) {
        return getPlayerDataId(playerId, landmarkName).thenApplyAsync(Optional::isPresent);
    }

    public CompletableFuture<Boolean> setPlayerLandmarkAvailable(UUID playerId,String landmarkName,boolean value){
        return supplyAsync(()->dbManager.setPlayerLandmarkAvailable(playerId,landmarkName,value));
    }
    @Contract("_ -> new")
    public static <U> @NotNull CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, playerDataExecutor);
    }
}
