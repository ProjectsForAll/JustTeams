package host.plas.justteams.timers;

import host.plas.justteams.managers.TeamManager;
import io.streamlined.bukkit.instances.BaseRunnable;

public class AutoCleanTimer extends BaseRunnable {
    public AutoCleanTimer() {
        super(5 * 20, 5 * 20, true);
    }

    @Override
    public void execute() {
        TeamManager.ensurePlayers();

        TeamManager.getPlayers().forEach(tagPlayer -> {
            tagPlayer.cleanMap();

            if (! tagPlayer.isOnline()) {
                TeamManager.unregisterPlayer(tagPlayer);
            }
        });
    }
}
