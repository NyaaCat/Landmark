package cat.nyaa.landmark.task;

import cat.nyaa.landmark.LandmarkPlugin;
import org.bukkit.Bukkit;

public class LandMarkTaskManager {
    private final int checkPlayer;
    private final LandmarkPlugin plugin;

    public LandMarkTaskManager(LandmarkPlugin landmarkPlugin){
        this.plugin = landmarkPlugin;
        this.checkPlayer = Bukkit.getScheduler().scheduleSyncRepeatingTask(landmarkPlugin,new TaskCheckPlayer(),1,1);
    }
    public void destructor(){
        Bukkit.getScheduler().cancelTask(checkPlayer);
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
