package host.plas.justteams;

import host.plas.justteams.commands.TeamMainCMD;
import host.plas.justteams.config.DatabaseConfig;
import host.plas.justteams.config.MainConfig;
import host.plas.justteams.database.TeamsDBOperator;
import host.plas.justteams.events.MainListener;
import host.plas.justteams.managers.TeamManager;
import host.plas.justteams.placeholders.PAPIExpansion;
import host.plas.justteams.timers.AutoCleanTimer;
import host.plas.justteams.timers.AutoSaveTimer;
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
public final class JustTeams extends PluginBase {
    @Getter @Setter
    private static JustTeams instance;
    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static DatabaseConfig databaseConfig;

    @Getter @Setter
    private static TeamsDBOperator mainDatabase;

    @Getter @Setter
    private static InventoryAPI guiApi;

    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static TeamMainCMD teamMainCMD;

    @Getter @Setter
    private static PAPIExpansion papiExpansion;

    @Getter @Setter
    private static AutoSaveTimer autoSaveTimer;
    @Getter @Setter
    private static AutoCleanTimer autoCleanTimer;

    public JustTeams() {
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
            setMainDatabase(new TeamsDBOperator(getDatabaseConfig().getConnectorSet()));
            Date now = new Date();
            getMainDatabase().loadAllTeams().whenComplete((bool, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                Date lapse = new Date();
                MessageUtils.logInfo("Loaded " + TeamManager.getTeams().size() + " teams into memory! Took " + (lapse.getTime() - now.getTime()) + " milliseconds!");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        setMainListener(new MainListener());

        setTeamMainCMD(new TeamMainCMD());

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

        TeamManager.getTeams().forEach(tag -> {
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
