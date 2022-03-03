package cat.nyaa.landmark.command;

import cat.nyaa.aolib.utils.CommandUtils;
import cat.nyaa.landmark.LandmarkI18n;
import cat.nyaa.landmark.LandmarkPlugin;
import cat.nyaa.landmark.db.landmark.Landmark;
import cat.nyaa.landmark.utils.UiUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LandmarkAdminCommand extends CommandReceiver {
    private final LandmarkPlugin plugin;
    private final ILocalizer i18n;

    public LandmarkAdminCommand(LandmarkPlugin plugin, LandmarkI18n _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
        this.i18n = _i18n;
    }

    @SubCommand(value = "add", permission = "landmark.admin.add", tabCompleter = "addTabCompleter")
    public void add(CommandSender sender, Arguments args) {
        String name = args.nextString();
        String world = args.nextString();
        var worldObj = Bukkit.getWorld(world);
        if (worldObj == null) {
            LandmarkI18n.send(sender, "command.world_not_found", world);
            return;
        }
        int x = args.nextInt();
        int y = args.nextInt();
        int z = args.nextInt();
        boolean nearbyActive = args.nextBoolean();
        boolean autoActive = args.nextBoolean();
        var landmarkManager = plugin.getLandmarkManager();
        if (landmarkManager == null) return;
        if (landmarkManager.getLandmark(name) != null) {
            LandmarkI18n.send(sender, "command.landmark_already_exist", name);
            return;
        }
        landmarkManager.setLandMark(new Landmark(name, worldObj.getName(), x, y, z, nearbyActive, autoActive, landmarkManager));
        LandmarkI18n.send(sender, "command.add.finish", name);
    }

    public List<String> addTabCompleter(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        switch (args.remains()) {
            case 1 -> {
                //name
                return Lists.newArrayList("<name>");
            }
            case 2 -> {
                //world
                return Lists.newArrayList(player.getWorld().getName());
            }
            case 3 -> {
                //X
                return Lists.newArrayList(String.valueOf(player.getLocation().getBlockX()));
            }
            case 4 -> {
                //y
                return Lists.newArrayList(String.valueOf(player.getLocation().getBlockY()));
            }
            case 5 -> {
                //z
                return Lists.newArrayList(String.valueOf(player.getLocation().getBlockZ()));
            }
            case 6, 7 -> {
                return Lists.newArrayList(String.valueOf(false));
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }


    @SubCommand(value = "remove", permission = "landmark.admin.remove")
    public void remove(CommandSender sender, Arguments args) {
        String name = args.nextString();
        var landmarkManager = plugin.getLandmarkManager();
        if (landmarkManager == null) return;
        if (landmarkManager.getLandmark(name) == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", name);
            return;
        }
        landmarkManager.removeLandmark(name);
        LandmarkI18n.send(sender, "command.remove.finish", name);

    }

    @SubCommand(value = "edit", permission = "landmark.admin.edit")
    public void edit(CommandSender sender, Arguments args) {
        String name = args.nextString();
        var landManager = plugin.getLandmarkManager();
        if (landManager == null) return;
        var landmark = landManager.getLandmark(name);
        if (landmark == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", name);
            return;
        }
        landmark.setX(args.argInt("x", landmark.getX()));
        landmark.setY(args.argInt("y", landmark.getY()));
        landmark.setZ(args.argInt("z", landmark.getZ()));
        landmark.setMark(args.argInt("mask-value", landmark.getMark()));

        landmark.setNearbyActive(Boolean.parseBoolean(args.argString("nearby-active", String.valueOf(landmark.getNearbyActive()))));
        landmark.setAutoActive(Boolean.parseBoolean(args.argString("auto-active", String.valueOf(landmark.getAutoActive()))));

        landmark.setDisplayName(args.argString("display-name", landmark.getDisplayName()));
        landmark.setIconItemString(args.argString("icon-item-base64", landmark.getIconItemString()));
        landmark.setDesc(args.argString("desc", landmark.getDesc()));
        landmark.saveDb();
        LandmarkI18n.send(sender, "command.edit.finish", name);
    }

    @SubCommand(value = "activate", permission = "landmark.admin.activate")
    public void activate(CommandSender sender, Arguments args) {
        var targetName = args.nextString();
        var targetUUID = CommandUtils.receiveCommand.getPlayerUUIDByStr(targetName, sender);
        if (targetUUID == null) {
            LandmarkI18n.send(sender, "command.invalid_target", targetName);
            return;
        }
        var landmarkName = args.nextString();

        var landMarkManager = plugin.getLandmarkManager();
        if (landMarkManager == null) return;
        var landmark = landMarkManager.getLandmark(landmarkName);
        if (landmark == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", landmarkName);
            return;
        }

        var playerDataManager = plugin.getPluginDataManager();
        if (playerDataManager == null) return;
        playerDataManager.isPlayerLandmarkAvailable(targetUUID, landmarkName).thenAcceptAsync((b) -> {
            if (!b) {
                playerDataManager.setPlayerLandmarkAvailable(targetUUID, landmarkName, true);
                LandmarkI18n.sendPlayerSync(targetUUID, "command.activate.success", landmarkName);
            } else {
                LandmarkI18n.sendPlayerSync(targetUUID, "command.activate.already_activated", landmarkName);
            }
        });
    }

    @SubCommand(value = "deactivate", permission = "landmark.admin.deactivate")
    public void deactivate(CommandSender sender, Arguments args) {
        var targetName = args.nextString();
        var targetUUID = CommandUtils.receiveCommand.getPlayerUUIDByStr(targetName, sender);
        if (targetUUID == null) {
            LandmarkI18n.send(sender, "command.invalid_target", targetName);
            return;
        }
        var landmarkName = args.nextString();


        var landMarkManager = plugin.getLandmarkManager();
        if (landMarkManager == null) return;
        var landmark = landMarkManager.getLandmark(landmarkName);
        if (landmark == null) {
            LandmarkI18n.send(sender, "command.landmark_not_found", landmarkName);
            return;
        }

        var playerDataManager = plugin.getPluginDataManager();
        if (playerDataManager == null) return;
        playerDataManager.isPlayerLandmarkAvailable(targetUUID, landmarkName).thenAcceptAsync((b) -> {
            if (b) {
                playerDataManager.setPlayerLandmarkAvailable(targetUUID, landmarkName, false);
                LandmarkI18n.sendPlayerSync(targetUUID, "command.deactivate.success", landmarkName);
            } else {
                LandmarkI18n.sendPlayerSync(targetUUID, "command.deactivate.not_yet_activated", landmarkName);
            }
        });
    }

    @SubCommand(value = "reload", permission = "landmark.admin.reload")
    public void reload(CommandSender sender, Arguments args) {
        plugin.onReload();
        LandmarkI18n.send(sender, "command.reload.success");
    }

    @SubCommand(value = "teleport", alias = {"tp"}, permission = "landmark.admin.teleport")
    public void teleport(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        String landmarkName = args.nextString();
        plugin.getLandmarkManagerOpt().ifPresent((l) -> l.teleportPlayerToLandmarkName(player.getUniqueId(), landmarkName));
    }

    @SubCommand(value = "menu", permission = "landmark.admin.menu")
    public void menu(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            LandmarkI18n.send(sender, "command.only-player-can-do");
            return;
        }
        UiUtils.openAdminMenu(player);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
