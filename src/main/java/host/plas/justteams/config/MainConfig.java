package host.plas.justteams.config;

import host.plas.justteams.JustTeams;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", JustTeams.getInstance(), true);
    }

    @Override
    public void init() {
        getPvpWorlds();
    }

    public ConcurrentSkipListSet<String> getPvpWorlds() {
        reloadResource();

        return new ConcurrentSkipListSet<>(getOrSetDefault("max.permission-prefix", new ArrayList<>(List.of("fightworld"))));
    }
}
