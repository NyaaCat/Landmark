package cat.nyaa.landmark.db.model;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.UUID;

@Table("playerLandmark")
public class PlayerDataDBModel {
    @Column(name = "id", primary = true, autoIncrement = true, nullable = true)
    public int id;
    @Column(name = "player")
    public UUID playerUniqueId;
    @Column(name = "landmark")
    public String landmarkName;
    public PlayerDataDBModel() {
    }
    public PlayerDataDBModel(int id, UUID playerUniqueId, String landmarkName) {
        this.id = id;
        this.playerUniqueId = playerUniqueId;
        this.landmarkName = landmarkName;
    }

    public static String getTableName() {
        return "playerLandmark";
    }
}
