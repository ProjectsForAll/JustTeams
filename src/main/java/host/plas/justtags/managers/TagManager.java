package host.plas.justtags.managers;

import host.plas.justtags.JustTags;
import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class TagManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<ConfiguredTag> tags = new ConcurrentSkipListSet<>();

    public static void registerTag(ConfiguredTag tag) {
        unregisterTag(tag);
        tags.add(tag);
    }

    public static void unregisterTag(ConfiguredTag tag) {
        unregisterTag(tag.getIdentifier());
    }

    public static void unregisterTag(String identifier) {
        getTags().removeIf(tag -> tag.getIdentifier().equals(identifier));
    }

    public static void unregisterAllTags() {
        getTags().forEach(ConfiguredTag::save);

        tags.clear();
    }

    public static Optional<ConfiguredTag> getTag(String identifier) {
        return tags.stream().filter(tag -> tag.getIdentifier().equals(identifier)).findFirst();
    }

    public static boolean isTagLoaded(String identifier) {
        return tags.stream().anyMatch(tag -> tag.getIdentifier().equals(identifier));
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<TagPlayer> players = new ConcurrentSkipListSet<>();

    public static void registerPlayer(TagPlayer player) {
        players.add(player);
    }

    public static void unregisterPlayer(TagPlayer player) {
        players.remove(player);
    }

    public static Optional<TagPlayer> getPlayer(String identifier) {
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

        getPlayers().forEach(TagPlayer::refresh);
    }

    public static void ensurePlayers() {
        CompletableFuture.runAsync(() -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (! isPlayerLoaded(player.getUniqueId().toString())) {
                    if (JustTags.getMainDatabase().playerExists(player.getUniqueId().toString()).join()) {
                        Optional<TagPlayer> optional = JustTags.getMainDatabase().loadPlayer(player.getUniqueId().toString()).join();
                        optional.ifPresent(TagManager::registerPlayer);
                    } else {
                        TagPlayer tagPlayer = new TagPlayer(player.getUniqueId().toString());
                        registerPlayer(tagPlayer);

                        tagPlayer.save();
                    }
                }
            });
        });
    }

    public static CompletableFuture<TagPlayer> loadOrCreatePlayerAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            boolean create = true;

            TagPlayer player = getPlayer(uuid).orElse(null);
            if (player != null) return player;

            if (JustTags.getMainDatabase().playerExists(uuid).join()) {
                create = false;
                Optional<TagPlayer> optional = JustTags.getMainDatabase().loadPlayer(uuid).join();
                optional.ifPresent(TagManager::registerPlayer);

                if (optional.isEmpty()) create = true;
                else player = optional.get();
            }

            if (create) {
                TagPlayer tagPlayer = new TagPlayer(uuid);
                registerPlayer(tagPlayer);

                tagPlayer.save();

                player = tagPlayer;
            }

            return player;
        });
    }
}
