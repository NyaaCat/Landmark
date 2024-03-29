package cat.nyaa.landmark;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;

import java.util.UUID;

public class LandmarkI18n extends LanguageRepository {
    private static LandmarkI18n instance;
    private final Plugin plugin;
    private final String lang;

    public LandmarkI18n(Plugin plugin, String lang) {
        instance = this;
        this.plugin = plugin;
        this.lang = lang;
        load();
    }

    @Contract(pure = true)
    public static String format(String key, Object... args) {
        if (instance == null) return "<Not initialized>";
        return instance.getFormatted(key, args);
    }

    @Contract(pure = true)
    public static String substitute(String key, Object... args) {
        if (instance == null) return "<Not initialized>";
        return instance.getSubstituted(key, args);
    }

    public static void sendPlayerSync(UUID playerId, String key, Object... args) {
        if (instance == null) return;
        TaskUtils.async.callSyncAndGet(() -> {
                    var player = Bukkit.getPlayer(playerId);
                    if (player == null) return null;
                    LandmarkI18n.send(player, key, args);
                    return null;
                }
        );
    }

    public static void send(CommandSender recipient, String key, Object... args) {
        recipient.sendMessage(format(key, args));
    }


    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    protected String getLanguage() {
        return this.lang;
    }
}
