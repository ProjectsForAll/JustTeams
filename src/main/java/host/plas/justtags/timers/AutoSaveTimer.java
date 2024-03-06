package host.plas.justtags.timers;

import host.plas.justtags.data.ConfiguredTeam;
import host.plas.justtags.data.TeamPlayer;
import host.plas.justtags.managers.TeamManager;
import io.streamlined.bukkit.instances.BaseRunnable;

public class AutoSaveTimer extends BaseRunnable {
    public AutoSaveTimer() {
        super(60 * 20, 60 * 20, true);
    }

    @Override
    public void execute() {
        TeamManager.getPlayers().forEach(TeamPlayer::save);
        TeamManager.getTags().forEach(ConfiguredTeam::save);
    }
}
