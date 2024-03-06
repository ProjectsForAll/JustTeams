package host.plas.justtags.timers;

import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import host.plas.justtags.managers.TagManager;
import io.streamlined.bukkit.instances.BaseRunnable;

public class AutoCleanTimer extends BaseRunnable {
    public AutoCleanTimer() {
        super(5 * 20, 5 * 20, true);
    }

    @Override
    public void execute() {
        TagManager.ensurePlayers();

        TagManager.getPlayers().forEach(tagPlayer -> {
            tagPlayer.cleanMap();

            if (! tagPlayer.isOnline()) {
                TagManager.unregisterPlayer(tagPlayer);
            }
        });
    }
}
