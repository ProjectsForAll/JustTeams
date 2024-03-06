package host.plas.justtags.config;

import host.plas.justtags.JustTags;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", JustTags.getInstance(), true);
    }

    @Override
    public void init() {
        getMaxPermissionPrefix();
    }

    public String getMaxPermissionPrefix() {
        reloadResource();

        return getOrSetDefault("max.permission-prefix", "justtags.max.");
    }

    public int getMaxDefault() {
        reloadResource();

        return getOrSetDefault("max.default", 3);
    }
}
