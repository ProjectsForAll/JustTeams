package host.plas.justtags.timers;

import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import host.plas.justtags.managers.TagManager;
import io.streamlined.bukkit.instances.BaseRunnable;

public class AutoSaveTimer extends BaseRunnable {
    public AutoSaveTimer() {
        super(60 * 20, 60 * 20, true);
    }

    @Override
    public void execute() {
        TagManager.getPlayers().forEach(TagPlayer::save);
        TagManager.getTags().forEach(ConfiguredTag::save);
    }
}
