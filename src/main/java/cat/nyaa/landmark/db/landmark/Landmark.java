package cat.nyaa.landmark.db.landmark;

import cat.nyaa.landmark.db.model.LandmarkDBModel;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Landmark {
    private final LandmarkDBModel model;
    private final LandMarkManager landMarkManager;
    private boolean dirty;
    private boolean removed;

    public Landmark(LandmarkDBModel model, LandMarkManager landMarkManager) {
        this.model = model;
        this.landMarkManager = landMarkManager;
    }

    public Landmark(String name, String worldName, int x, int y, int z, boolean nearbyActive, boolean autoActive, LandMarkManager landMarkManager) {
        this(new LandmarkDBModel(name, worldName, x, y, z, newMark(nearbyActive, autoActive), null, null, null), landMarkManager);
    }

    private static int newMark(boolean nearbyActive, boolean autoActive) {
        int result = 0;
        if (nearbyActive) {
            result = result | 0b1;
        }
        if (autoActive) {
            result = result | 0b10;
        }
        return result;
    }


    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Landmark fromModel(LandmarkDBModel model, LandMarkManager landMarkManager) {
        return new Landmark(model, landMarkManager);
    }

    public String getName() {
        return model.name;
    }

    public LandmarkDBModel getModel() {
        return model;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(model.world), model.x, model.y, model.z);
    }

    public String getWorldName() {
        return model.world;
    }

    public int getX() {
        return model.x;
    }

    public int getY() {
        return model.y;
    }

    public int getZ() {
        return model.z;
    }

    public int getMark() {
        return model.mark;
    }

    public boolean getNearbyActive() {
        return (getMark() & 0b1) > 0;
    }

    public boolean getAutoActive() {
        return (getMark() & 0b10) > 0;
    }

    @Nullable
    public String getDisplayName() {
        return model.displayName;
    }

    @Nullable
    public String getIconItemString() {
        return model.iconItemBase64;
    }

    public ItemStack getIconItem() {
        String base64 = this.getIconItemString();
        if (base64 == null || base64.isEmpty()) {
            return genNewIconItem();
        }
        var item = ItemStackUtils.itemFromBase64(base64);
        if (item == null) return genNewIconItem();
        return item;
    }

    private ItemStack genNewIconItem() {
        var item = new ItemStack(Material.MAP);
        var mata = item.getItemMeta();
        if (mata == null) return item;
        if (this.getDisplayName() != null) {
            mata.setDisplayName(this.getDisplayName());
        } else {
            mata.setDisplayName(this.getName());
        }
        if (this.getDesc() != null) {
            mata.setLore(Lists.newArrayList(this.getDesc()));
        }
        item.setItemMeta(mata);
        return item;
    }

    @Nullable
    public String getDesc() {
        return model.desc;
    }

    public void setX(int v) {
        model.x = v;
    }

    public void setY(int v) {
        model.y = v;
    }

    public void setZ(int v) {
        model.z = v;
    }

    public void setMark(int v) {
        model.mark = v;
    }

    public void setNearbyActive(boolean v) {
        if (getNearbyActive() == v) return;
        setMark(getMark() ^ 0b1);
    }

    public void setAutoActive(boolean v) {
        if (getAutoActive() == v) return;
        setMark(getMark() ^ 0b10);
    }

    public void setDisplayName(@Nullable String v) {
        model.displayName = v;
    }

    public void setIconItemString(@Nullable String v) {
        model.iconItemBase64 = v;
    }


    public void setDesc(@Nullable String v) {
        model.desc = v;
    }

    public void saveDb() {
        landMarkManager.setLandMark(this);
    }
}
