package host.plas.justteams.managers;

import host.plas.justteams.JustTeams;
import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class TeamManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<ConfiguredTeam> teams = new ConcurrentSkipListSet<>();

    public static void registerTeam(ConfiguredTeam tag) {
        unregisterTeam(tag);
        teams.add(tag);
    }

    public static void unregisterTeam(ConfiguredTeam tag) {
        unregisterTeam(tag.getIdentifier());
    }

    public static void unregisterTeam(String identifier) {
        getTeams().removeIf(tag -> tag.getIdentifier().equals(identifier));
    }

    public static void unregisterAllTeams() {
        getTeams().forEach(ConfiguredTeam::save);

        teams.clear();
    }

    public static Optional<ConfiguredTeam> getTeam(String identifier) {
        return teams.stream().filter(tag -> tag.getIdentifier().equals(identifier)).findFirst();
    }

    public static boolean isTeamLoaded(String identifier) {
        return teams.stream().anyMatch(tag -> tag.getIdentifier().equals(identifier));
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<TeamPlayer> players = new ConcurrentSkipListSet<>();

    public static void registerPlayer(TeamPlayer player) {
        players.add(player);
    }

    public static void unregisterPlayer(TeamPlayer player) {
        players.remove(player);
    }

    public static Optional<TeamPlayer> getPlayer(String identifier) {
        return players.stream().filter(player -> player.getIdentifier().equals(identifier)).findFirst();
    }

    public static boolean isPlayerLoaded(String identifier) {
        return players.stream().anyMatch(player -> player.getIdentifier().equals(identifier));
    }

    public static void refreshPlayers() {
        ensurePlayers();

        getPlayers().forEach(TeamPlayer::refresh);
    }

    public static void ensurePlayers() {
        CompletableFuture.runAsync(() -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (! isPlayerLoaded(player.getUniqueId().toString())) {
                    if (JustTeams.getMainDatabase().playerExists(player.getUniqueId().toString()).join()) {
                        Optional<TeamPlayer> optional = JustTeams.getMainDatabase().loadPlayer(player.getUniqueId().toString()).join();
                        optional.ifPresent(TeamManager::registerPlayer);
                    } else {
                        TeamPlayer teamPlayer = new TeamPlayer(player.getUniqueId().toString());
                        registerPlayer(teamPlayer);

                        teamPlayer.save();
                    }
                }
            });
        });
    }

    public static CompletableFuture<TeamPlayer> loadOrCreatePlayerAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            boolean create = true;

            TeamPlayer player = getPlayer(uuid).orElse(null);
            if (player != null) return player;

            if (JustTeams.getMainDatabase().playerExists(uuid).join()) {
                create = false;
                Optional<TeamPlayer> optional = JustTeams.getMainDatabase().loadPlayer(uuid).join();
                optional.ifPresent(TeamManager::registerPlayer);

                if (optional.isEmpty()) create = true;
                else player = optional.get();
            }

            if (create) {
                TeamPlayer teamPlayer = new TeamPlayer(uuid);
                registerPlayer(teamPlayer);

                teamPlayer.save();

                player = teamPlayer;
            }

            return player;
        });
    }
}
