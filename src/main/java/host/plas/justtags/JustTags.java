package host.plas.justtags;

import host.plas.justtags.commands.TagMainCMD;
import host.plas.justtags.config.DatabaseConfig;
import host.plas.justtags.config.MainConfig;
import host.plas.justtags.database.TagsDBOperator;
import host.plas.justtags.events.MainListener;
import host.plas.justtags.managers.TeamManager;
import host.plas.justtags.placeholders.PAPIExpansion;
import host.plas.justtags.timers.AutoCleanTimer;
import host.plas.justtags.timers.AutoSaveTimer;
import io.streamlined.bukkit.MessageUtils;
import io.streamlined.bukkit.PluginBase;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public final class JustTags extends PluginBase {
    @Getter @Setter
    private static JustTags instance;
    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static DatabaseConfig databaseConfig;

    @Getter @Setter
    private static TagsDBOperator mainDatabase;

    @Getter @Setter
    private static InventoryAPI guiApi;

    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static TagMainCMD tagMainCMD;

    @Getter @Setter
    private static PAPIExpansion papiExpansion;

    @Getter @Setter
    private static AutoSaveTimer autoSaveTimer;
    @Getter @Setter
    private static AutoCleanTimer autoCleanTimer;

    public JustTags() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        setInstance(this);

        setMainConfig(new MainConfig());
        setDatabaseConfig(new DatabaseConfig());

        setGuiApi(new InventoryAPI(this));
        getGuiApi().init();

        try {
            setMainDatabase(new TagsDBOperator(getDatabaseConfig().getConnectorSet()));
            Date now = new Date();
            getMainDatabase().loadAllTags().whenComplete((bool, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                Date lapse = new Date();
                MessageUtils.logInfo("Loaded " + TeamManager.getTags().size() + " tags into memory! Took " + (lapse.getTime() - now.getTime()) + " milliseconds!");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        setMainListener(new MainListener());

        setTagMainCMD(new TagMainCMD());

        setPapiExpansion(new PAPIExpansion());
        getPapiExpansion().register();

        setAutoSaveTimer(new AutoSaveTimer());
        setAutoCleanTimer(new AutoCleanTimer());
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic

        TeamManager.getPlayers().forEach(player -> {
            player.save(false);
            player.unregister();
        });

        TeamManager.getTags().forEach(tag -> {
            tag.save();
            tag.unregister();
        });
    }

    /**
     * Get a map of online players.
     * Sorted by player name.
     * @return A map of online players sorted by player name.
     */
    public static ConcurrentSkipListMap<String, Player> getOnlinePlayers() {
        ConcurrentSkipListMap<String, Player> onlinePlayers = new ConcurrentSkipListMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayers.put(player.getName(), player);
        }

        return onlinePlayers;
    }
}
