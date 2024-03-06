package host.plas.justteams.timers;

import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.managers.TeamManager;
import io.streamlined.bukkit.instances.BaseRunnable;

public class AutoSaveTimer extends BaseRunnable {
    public AutoSaveTimer() {
        super(60 * 20, 60 * 20, true);
    }

    @Override
    public void execute() {
        TeamManager.getPlayers().forEach(TeamPlayer::save);
        TeamManager.getTeams().forEach(ConfiguredTeam::save);
    }
}
