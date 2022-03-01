package cat.nyaa.landmark.db.model;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Table("landmark")
public class LandmarkDBModel {
    public LandmarkDBModel() {
    }

    @Column(name = "name", primary = true)
    @NotNull
    public String name;
    @Column(name = "world")
    @NotNull
    public String world;
    @Column(name = "x")
    public int x;
    @Column(name = "y")
    public int y;
    @Column(name = "z")
    public int z;
    @Column(name = "mark")
    public int mark;
    @Column(name = "display", nullable = true)
    @Nullable
    public String displayName;
    @Column(name = "item", nullable = true)
    @Nullable
    public String iconItemBase64;
    @Column(name = "desc", nullable = true)
    @Nullable
    public String desc;

    public LandmarkDBModel(@NotNull String name, @NotNull String world, int x, int y, int z, int mark, @Nullable String display, @Nullable String item, @Nullable String desc) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.mark = mark;
        this.displayName = display;
        this.iconItemBase64 = item;
        this.desc = desc;
    }


    public static String getTableName() {
        return "landmark";
    }
}
