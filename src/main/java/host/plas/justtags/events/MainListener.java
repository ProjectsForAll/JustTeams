package host.plas.justtags.events;

import host.plas.justtags.JustTags;
import host.plas.justtags.managers.TagManager;
import host.plas.justtags.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MainListener implements Listener {
    public MainListener() {
        Bukkit.getPluginManager().registerEvents(this, JustTags.getInstance());

        MessageUtils.logInfo("Registered MainListener!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        TagManager.loadOrCreatePlayerAsync(player.getUniqueId().toString());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        TagManager.loadOrCreatePlayerAsync(player.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            tagPlayer.save();
            tagPlayer.unregister();
        });
    }
}
