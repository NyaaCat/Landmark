package cat.nyaa.landmark.db;

import cat.nyaa.landmark.config.LandmarkDatabaseConfig;
import cat.nyaa.landmark.db.model.LandmarkDBModel;
import cat.nyaa.landmark.db.model.PlayerDataDBModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class landmarkDbManager {
    private final LandmarkDatabaseConfig databaseConfig;
    public static final UUID lock = UUID.randomUUID();

    public landmarkDbManager(@NotNull LandmarkDatabaseConfig landmarkDatabaseConfig) {
        this.databaseConfig = landmarkDatabaseConfig;
        loadTables();
    }

    private void loadTables() {
        var res = databaseConfig.getPlugin().getResource("sql/init.sql");
        if (res == null) return;
        var optConn = newJdbcConnection();
        if (optConn.isEmpty()) return;
        try (var conn = optConn.get()) {
            try (var statement = conn.createStatement()) {
                statement.executeUpdate(new String(res.readAllBytes()));
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<PlayerDataDBModel> getPlayerData(UUID playerId) {
        synchronized (lock) {
            var result = new ArrayList<PlayerDataDBModel>();
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return result;
            try (
                    var conn = optConn.get();
                    var ps = conn.prepareStatement("SELECT * from " + PlayerDataDBModel.getTableName() + " WHERE player=?")
            ) {
                ps.setString(1, playerId.toString());
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String landmark = rs.getString("landmark");
                        UUID player = UUID.fromString(rs.getString("player"));
                        result.add(new PlayerDataDBModel(id, player, landmark));

                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return result;
        }
    }

    Optional<PlayerDataDBModel> getPlayerData(int id) {
        synchronized (lock) {
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return Optional.empty();
            try (
                    var conn = optConn.get();
                    var ps = conn.prepareStatement("SELECT * FROM " + PlayerDataDBModel.getTableName() + " WHERE id=?")
            ) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        return Optional.ofNullable(rs2playerDataModel(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }

    public boolean setPlayerLandmarkAvailable(UUID playerId, String landmarkName, boolean value) {
        synchronized (lock) {
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return false;
            Optional<Integer> playerDataIdOpt;
            try (var conn = optConn.get()) {
                playerDataIdOpt = getPlayerDataId_(conn, playerId, landmarkName);
                if (value) {
                    if (playerDataIdOpt.isEmpty()) {
                        return (insertPlayerData_(conn, playerId, landmarkName) >= 1);
                    }
                } else {
                    if (playerDataIdOpt.isPresent()) {
                        return (removePlayerData_(conn, playerId, landmarkName) >= 1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
    }

    private int removePlayerData_(@NotNull Connection conn, @NotNull UUID playerId, String landmarkName) {
        try (var ps = conn.prepareStatement("DELETE FROM " + PlayerDataDBModel.getTableName() + " WHERE (player=? AND landmark=?)")) {
            ps.setString(1, playerId.toString());
            ps.setString(2, landmarkName);
            return ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    private int insertPlayerData_(Connection conn, UUID playerId, String landmarkName) throws SQLException {
        try (var ps = conn.prepareStatement("INSERT INTO " + PlayerDataDBModel.getTableName() + " (player,landmark) VALUES (?,?)")) {
            ps.setString(1, playerId.toString());
            ps.setString(2, landmarkName);
            return ps.executeUpdate();
        }
    }

    public Optional<Integer> getPlayerDataId(UUID playerId, String landmarkName) {
        synchronized (lock) {
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return Optional.empty();
            try (var conn = optConn.get()) {
                return getPlayerDataId_(conn, playerId, landmarkName);
            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }

    private Optional<Integer> getPlayerDataId_(Connection conn, @NotNull UUID playerId, String landmarkName) throws SQLException {
        try (var ps = conn.prepareStatement("SELECT id FROM " + PlayerDataDBModel.getTableName() + " WHERE (player=? AND landmark =?)")) {
            ps.setString(1, playerId.toString());
            ps.setString(2, landmarkName);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    return Optional.of(rs.getInt("id"));
                }
            }
        }
        return Optional.empty();

    }

    public int insertOrUpdateLandmark(LandmarkDBModel model) {
        synchronized (lock) {
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return 0;
            try (var conn = optConn.get()) {

                if (hasLandmarkName_(conn, model.name)) {
                    return updateLandmark_(conn, model);
                } else {
                    return insertLandmarkModel_(conn, model);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return 0;
            }
        }
    }

    private boolean hasLandmarkName_(Connection conn, String name) throws SQLException {
        try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM " + LandmarkDBModel.getTableName() + " WHERE name=?")) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private int updateLandmark_(Connection conn, @NotNull LandmarkDBModel model) throws SQLException {
        try (
                var ps = conn.prepareStatement("UPDATE FROM " + LandmarkDBModel.getTableName() + " SET name = ?, world = ?, x = ?, y = ?, z = ?, mark = ?, display = ?, item = ?, desc = ? WHERE name = ?")
        ) {
            ps.setObject(1, model.name);
            ps.setObject(2, model.world);
            ps.setObject(3, model.x);
            ps.setObject(4, model.y);
            ps.setObject(5, model.z);
            ps.setObject(6, model.mark);
            ps.setObject(7, model.displayName);
            ps.setObject(8, model.iconItemBase64);
            ps.setObject(9, model.desc);
            ps.setObject(10, model.name);
            return ps.executeUpdate();
        }

    }

    private int insertLandmarkModel_(Connection conn, @NotNull LandmarkDBModel model) throws SQLException {
        try (
                var ps = conn.prepareStatement("INSERT INTO " + LandmarkDBModel.getTableName() + " (name,world,x,y,z,mark,display,item,desc) VALUES (?,?,?,?,?,?,?,?,?)")
        ) {
            ps.setObject(1, model.name);
            ps.setObject(2, model.world);
            ps.setObject(3, model.x);
            ps.setObject(4, model.y);
            ps.setObject(5, model.z);
            ps.setObject(6, model.mark);
            ps.setObject(7, model.displayName);
            ps.setObject(8, model.iconItemBase64);
            ps.setObject(9, model.desc);
            return ps.executeUpdate();
        }
    }

    public int removeLandmark(String name) {
        synchronized (lock) {
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return 0;
            try (
                    var conn = optConn.get();
                    var ps = conn.prepareStatement("DELETE FROM " + LandmarkDBModel.getTableName() + " WHERE name=?")
            ) {
                ps.setObject(1, name);
                return ps.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return 0;
            }
        }
    }

    public List<LandmarkDBModel> getAllLandmarkModel() {
        synchronized (lock) {
            var result = new ArrayList<LandmarkDBModel>();
            var optConn = newJdbcConnection();
            if (optConn.isEmpty()) return result;
            try (
                    var conn = optConn.get();
                    var ps = conn.prepareStatement("SELECT * FROM " + LandmarkDBModel.getTableName());
                    var rs = ps.executeQuery()
            ) {
                while (rs.next()) {
                    var model = rs2landmarkModel(rs);
                    if (model != null) result.add(model);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return result;
        }
    }

    private @Nullable PlayerDataDBModel rs2playerDataModel(@NotNull ResultSet rs) {
        try {
            int id = rs.getInt("id");
            UUID playerUniqueId = UUID.fromString(rs.getString("player"));
            String landmarkName = rs.getString("landmark");
            return new PlayerDataDBModel(id, playerUniqueId, landmarkName);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private @Nullable LandmarkDBModel rs2landmarkModel(@NotNull ResultSet rs) {
        try {
            String name = rs.getString("name");
            String world = rs.getString("world");
            int x = rs.getInt("x");
            int y = rs.getInt("y");
            int z = rs.getInt("z");
            int mark = rs.getInt("mark");
            @Nullable String display = readNullableString(rs.getObject("display"));
            @Nullable String item = readNullableString(rs.getObject("item"));
            @Nullable String desc = readNullableString(rs.getObject("desc"));
            return new LandmarkDBModel(name, world, x, y, z, mark, display, item, desc);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    private @Nullable String readNullableString(@Nullable Object o) {
        if (o instanceof String) return (String) o;
        return null;
    }

    Optional<Connection> newJdbcConnection() {
        File f = new File(databaseConfig.getPlugin().getDataFolder(), databaseConfig.sqlite_file);
        Connection result = null;
        try {
            result = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
            result.setAutoCommit(true);
            return Optional.of(result);
        } catch (SQLException throwables) {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException ignored) {
                }
            }
            throwables.printStackTrace();
        }
        return Optional.empty();
    }


}
