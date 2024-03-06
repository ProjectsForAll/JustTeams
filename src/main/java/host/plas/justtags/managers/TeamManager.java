package host.plas.justtags.managers;

import host.plas.justtags.JustTags;
import host.plas.justtags.data.ConfiguredTeam;
import host.plas.justtags.data.TeamPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class TeamManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<ConfiguredTeam> tags = new ConcurrentSkipListSet<>();

    public static void registerTeam(ConfiguredTeam tag) {
        unregisterTeam(tag);
        tags.add(tag);
    }

    public static void unregisterTeam(ConfiguredTeam tag) {
        unregisterTeam(tag.getIdentifier());
    }

    public static void unregisterTeam(String identifier) {
        getTags().removeIf(tag -> tag.getIdentifier().equals(identifier));
    }

    public static void unregisterAllTeams() {
        getTags().forEach(ConfiguredTeam::save);

        tags.clear();
    }

    public static Optional<ConfiguredTeam> getTeam(String identifier) {
        return tags.stream().filter(tag -> tag.getIdentifier().equals(identifier)).findFirst();
    }

    public static boolean isTeamLoaded(String identifier) {
        return tags.stream().anyMatch(tag -> tag.getIdentifier().equals(identifier));
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

    public static void putTag(String identifier, int index, String value) {
        getPlayer(identifier).ifPresent(player -> player.putTag(index, value));
    }

    public static void removeTag(String identifier, int index) {
        getPlayer(identifier).ifPresent(player -> player.removeTag(index));
    }

    public static void refreshPlayers() {
        ensurePlayers();

        getPlayers().forEach(TeamPlayer::refresh);
    }

    public static void ensurePlayers() {
        CompletableFuture.runAsync(() -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (! isPlayerLoaded(player.getUniqueId().toString())) {
                    if (JustTags.getMainDatabase().playerExists(player.getUniqueId().toString()).join()) {
                        Optional<TeamPlayer> optional = JustTags.getMainDatabase().loadPlayer(player.getUniqueId().toString()).join();
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

            if (JustTags.getMainDatabase().playerExists(uuid).join()) {
                create = false;
                Optional<TeamPlayer> optional = JustTags.getMainDatabase().loadPlayer(uuid).join();
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
