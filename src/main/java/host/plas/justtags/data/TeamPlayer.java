package host.plas.justtags.data;

import host.plas.justtags.JustTags;
import host.plas.justtags.managers.TeamManager;
import io.streamlined.bukkit.lib.thebase.lib.re2j.Matcher;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tv.quaint.objects.Identifiable;
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Getter @Setter
public class TeamPlayer implements Identifiable {
    public UUID getUuid() {
        return UUID.fromString(identifier);
    }

    public void setUuid(UUID uuid) {
        identifier = uuid.toString();
    }

    private String identifier;
    private Optional<ConfiguredTeam> chosenTeam;

    public TeamPlayer(String uuid) {
        this.identifier = uuid;
        this.chosenTeam = Optional.empty();
    }

    public TeamPlayer selectTeam(ConfiguredTeam team) {
        this.chosenTeam = Optional.of(team);
        return this;
    }

    public TeamPlayer unselectTeam() {
        this.chosenTeam = Optional.empty();
        return this;
    }

    public TeamPlayer ifTeam(Consumer<ConfiguredTeam> team) {
        chosenTeam.ifPresent(team);
        return this;
    }

    public boolean hasTag(ConfiguredTeam tag) {
        return container.containsValue(tag);
    }

    public boolean hasTag(String tag5) {
        return container.entrySet().stream().anyMatch(entry -> entry.getValue().getIdentifier().equals(tag5));
    }

    public void clearTags() {
        container.clear();
    }

    public void concatMap() {
        cleanMap();

        ConcurrentSkipListMap<Integer, ConfiguredTeam> cloned = new ConcurrentSkipListMap<>(getContainer());
        AtomicInteger index = new AtomicInteger(0);

        clearTags();

        cloned.forEach((key, value) -> {
            if (hasTag(value.getIdentifier())) return;
            putTagHard(index.getAndIncrement(), value);
        });

        trimMap();
    }

    public void trimMap() {
        ConcurrentSkipListMap<Integer, ConfiguredTeam> cloned = new ConcurrentSkipListMap<>(getContainer());
        AtomicInteger index = new AtomicInteger(0);

        clearTags();

        cloned.entrySet().stream().filter(entry -> entry.getKey() <= getFinalMaxTags()).forEach((entry) -> {
            if (hasTag(entry.getValue().getIdentifier())) return;
            putTagHard(index.getAndIncrement(), entry.getValue());
        });
    }

    public void cleanMap() {
        cleanAvailable();

        ConcurrentSkipListMap<Integer, ConfiguredTeam> cloned = new ConcurrentSkipListMap<>(getContainer());
        AtomicInteger index = new AtomicInteger(0);

        clearTags();

        cloned.entrySet().stream().filter(entry -> hasAvailableTag(entry.getValue().getIdentifier())).forEach((entry) -> {
            if (hasTag(entry.getValue().getIdentifier())) return;
            putTagHard(index.getAndIncrement(), entry.getValue());
        });
    }

    public void cleanAvailable() {
        ConcurrentSkipListSet<ConfiguredTeam> cloned = new ConcurrentSkipListSet<>(getAvailable());

        clearAvailableTags();

        cloned.stream().filter(tag -> {
            if (getConfiguredTag(tag.getIdentifier()).isPresent()) return true;
            return JustTags.getMainDatabase().tagExists(tag.getIdentifier()).join();
        }).forEach(this::addAvailableTag);
    }

    public Optional<ConfiguredTeam> getTag(int index) {
        ConfiguredTeam found = container.get(index);

        return found == null ? Optional.empty() : Optional.of(found);
    }

    public Optional<ConfiguredTeam> getTag(String identifier) {
        return container.values().stream().filter(tag -> tag.getIdentifier().equals(identifier)).findFirst();
    }

    public void addAvailableTag(ConfiguredTeam tag) {
        available.add(tag);
    }

    public void addAvailableTag(String identifier) {
        getConfiguredTag(identifier).ifPresent(this::addAvailableTag);
    }

    public void removeAvailableTag(ConfiguredTeam tag) {
        available.remove(tag);
    }

    public void removeAvailableTag(String identifier) {
        getConfiguredTag(identifier).ifPresent(this::removeAvailableTag);
    }

    public boolean hasAvailableTag(ConfiguredTeam tag) {
        return available.contains(tag);
    }

    public boolean hasAvailableTag(String identifier) {
        return available.stream().anyMatch(tag -> tag.getIdentifier().equals(identifier));
    }

    public Optional<ConfiguredTeam> getAvailableTag(String identifier) {
        return available.stream().filter(tag -> tag.getIdentifier().equals(identifier)).findFirst();
    }

    public Optional<ConfiguredTeam> getAvailableTag(int index) {
        AtomicReference<Optional<ConfiguredTeam>> tag = new AtomicReference<>(Optional.empty());

        AtomicInteger i = new AtomicInteger(0);
        available.forEach(configuredTag -> {
            if (i.get() == index) {
                tag.set(Optional.of(configuredTag));
            }

            i.getAndIncrement();
        });

        return tag.get();
    }

    public void clearAvailableTags() {
        available.clear();
    }

    public String getComputableAvailableTags() {
        StringBuilder builder = new StringBuilder();

        available.forEach(tag -> builder.append("!!!").append(tag.getIdentifier()).append(";;;"));

        return builder.toString();
    }

    public void computeAvailableTags(String computable) {
        Matcher matcher = MatcherUtils.matcherBuilder("[!][!][!](.*?)[;][;][;]", computable);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 1);

        clearAvailableTags();

        for (String[] group : groups) {
            getConfiguredTag(group[0]).ifPresent(this::addAvailableTag);
        }
    }

    public Optional<Player> asPlayer() {
        Player player = Bukkit.getPlayer(getUuid());

        return player == null ? Optional.empty() : Optional.of(player);
    }

    public boolean isOnline() {
        return asPlayer().isPresent() && asPlayer().get().isOnline();
    }

    public int getFinalMaxTags() {
        int perm = maxPermableTags();
        int def = getMaxDefault();

        return Math.max(perm, def);
    }

    public int maxPermableTags() {
        if (! isOnline()) return 0;
        Player player = asPlayer().get();

        int max = getHardCapMax();
        while (! player.hasPermission(getMaxPermissionPrefix() + max)) {
            max --;

            if (max <= 0) break; // max is 0
        }

        return max;
    }

    public void ifPlayer(Consumer<Player> consumer) {
        asPlayer().ifPresent(consumer);
    }

    public String getContainerString() {
        concatMap();

        StringBuilder builder = new StringBuilder();

        int m = Math.min(getFinalMaxTags(), getContainer().size());
        for (int i = 0; i < m; i++) {
            Optional<ConfiguredTeam> tag = getTag(i);
            tag.ifPresent(configuredTag -> builder.append(configuredTag.getValue()));
        }

        return builder.toString();
    }

    public String getComputableContainer() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < getContainer().size(); i++) {
            Optional<ConfiguredTeam> tag = getTag(i);

            int finalI = i;
            tag.ifPresent(configuredTag -> builder.append("!!!").append(finalI).append("->->").append(configuredTag.getIdentifier()).append(";;;"));
        }

        return builder.toString();
    }

    public void computeContainer(String computable) {
        Matcher matcher = MatcherUtils.matcherBuilder("[!][!][!](.*?)[-][>][-][>](.*?)[;][;][;]", computable);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 2);

        for (String[] group : groups) {
            int i = 0;
            try {
                i = Integer.parseInt(group[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            putTag(i, group[1]);
        }
    }

    public void save(boolean async) {
        if (async) JustTags.getMainDatabase().savePlayer(this);
        else JustTags.getMainDatabase().savePlayer(this).join();
    }

    public void save() {
        save(true);
    }

    public void register() {
        TeamManager.registerPlayer(this);
    }

    public void unregister() {
        TeamManager.unregisterPlayer(this);
    }

    public void refreshTagInstances() {
        container.forEach((index, tag) -> {
            Optional<ConfiguredTeam> configuredTag = getConfiguredTag(tag.getIdentifier());
            if (configuredTag.isPresent()) {
                putTagHard(index, configuredTag.get());
            } else {
                removeTag(index);
            }
        });

        concatMap();
    }

    public void refresh() {
        refreshTagInstances();
        save();
    }

    public void insertTag(int index, ConfiguredTeam tag) {
        if (getContainer().containsKey(index)) {
            ConfiguredTeam replaced = getContainer().replace(index, tag);
            while (replaced != null) {
                index ++;
                replaced = getContainer().replace(index, tag);
            }
        } else {
            getContainer().put(index, tag);
        }

        concatMap();
    }

    public void insertTag(int index, String identifier) {
        getConfiguredTag(identifier).ifPresent(tag -> insertTag(index, tag));
    }

    public static int getHardCapMax() {
        return Math.max(getMaxDefault(), 500);
    }

    public static String getMaxPermissionPrefix() {
        return JustTags.getMainConfig().getMaxPermissionPrefix();
    }

    public static int getMaxDefault() {
        return JustTags.getMainConfig().getMaxDefault();
    }

    public static Optional<ConfiguredTeam> getConfiguredTag(String identifier) {
        return TeamManager.getTeam(identifier);
    }
}
