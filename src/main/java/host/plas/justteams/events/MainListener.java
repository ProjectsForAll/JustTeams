package host.plas.justteams.events;

import host.plas.justteams.JustTeams;
import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.managers.TeamManager;
import host.plas.justteams.utils.MessageUtils;
import io.streamlined.bukkit.commands.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class MainListener implements Listener {
    public MainListener() {
        Bukkit.getPluginManager().registerEvents(this, JustTeams.getInstance());

        MessageUtils.logInfo("Registered MainListener!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        TeamManager.loadOrCreatePlayerAsync(player.getUniqueId().toString());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        TeamManager.loadOrCreatePlayerAsync(player.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            tagPlayer.save();
            tagPlayer.unregister();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (! (damager instanceof Player)) return;
        if (! (damaged instanceof Player)) return;

        Player damagerPlayer = (Player) damager;
        Player damagedPlayer = (Player) damaged;

        handleDamage(event, damagerPlayer, damagedPlayer);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayerProjectile(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (! (damager instanceof Projectile)) return;
        if (! (damaged instanceof Player)) return;

        Projectile damagerProjectile = (Projectile) damager;
        Player damagedPlayer = (Player) damaged;

        ProjectileSource source = damagerProjectile.getShooter();
        if (! (source instanceof Player)) return;

        Player damagerPlayer = (Player) source;

        handleDamage(event, damagerPlayer, damagedPlayer);
    }

    public static void handleDamage(Cancellable event, Player damagerPlayer, Player damagedPlayer) {
        TeamPlayer damagerPVPPlayer = TeamManager.loadOrCreatePlayerAsync(damagerPlayer.getUniqueId().toString()).join();
        TeamPlayer damagedPVPPlayer = TeamManager.loadOrCreatePlayerAsync(damagedPlayer.getUniqueId().toString()).join();

        Optional<ConfiguredTeam> damagerTeam = damagerPVPPlayer.getChosenTeam();
        if (damagerTeam.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        Optional<ConfiguredTeam> damagedTeam = damagedPVPPlayer.getChosenTeam();
        if (damagedTeam.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        ConfiguredTeam damagerTeamObj = damagerTeam.get();
        ConfiguredTeam damagedTeamObj = damagedTeam.get();

        if (damagedTeamObj.getIdentifier().equals(damagerTeamObj.getIdentifier())) {
            event.setCancelled(true);

            Sender damagerSender = new Sender(damagerPlayer);
            damagerSender.sendMessage("&cYou cannot PVP players in your same team!");
        } else {
            ConcurrentSkipListSet<String> pvpWorlds = JustTeams.getMainConfig().getPvpWorlds();
            if (! pvpWorlds.contains(damagedPlayer.getWorld().getName())) return;

            event.setCancelled(false);
        }
    }
}
