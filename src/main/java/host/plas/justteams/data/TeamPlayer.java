package host.plas.justteams.data;

import host.plas.justteams.JustTeams;
import host.plas.justteams.managers.TeamManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tv.quaint.objects.Identifiable;

import java.util.Optional;
import java.util.UUID;
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

    public TeamPlayer selectTeam(String team) {
        getConfiguredTeam(team).ifPresent(this::selectTeam);
        return this;
    }

    public TeamPlayer unselectTeam() {
        this.chosenTeam = Optional.empty();
        return this;
    }

    public TeamPlayer unselectTeam(ConfiguredTeam team) {
        return unselectTeam(team.getIdentifier());
    }

    public TeamPlayer unselectTeam(String team) {
        if (! hasTeam(team)) return this;

        this.chosenTeam = Optional.empty();
        return this;
    }

    public TeamPlayer ifTeam(Consumer<ConfiguredTeam> team) {
        chosenTeam.ifPresent(team);
        return this;
    }

    public boolean hasTeam(ConfiguredTeam team) {
        return hasTeam(team.getIdentifier());
    }

    public boolean hasTeam(String team) {
        return chosenTeam.isPresent() && chosenTeam.get().getIdentifier().equals(team);
    }

    public Optional<Player> asPlayer() {
        Player player = Bukkit.getPlayer(getUuid());

        return player == null ? Optional.empty() : Optional.of(player);
    }

    public boolean isOnline() {
        return asPlayer().isPresent() && asPlayer().get().isOnline();
    }

    public void ifPlayer(Consumer<Player> consumer) {
        asPlayer().ifPresent(consumer);
    }

    public void save(boolean async) {
        if (async) JustTeams.getMainDatabase().savePlayer(this);
        else JustTeams.getMainDatabase().savePlayer(this).join();
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

    public void refreshTeamInstance() {
        if (chosenTeam.isPresent()) {
            chosenTeam = getConfiguredTeam(chosenTeam.get().getIdentifier());
        }
    }

    public void refresh() {
        refreshTeamInstance();
        save();
    }

    public static Optional<ConfiguredTeam> getConfiguredTeam(String identifier) {
        return TeamManager.getTeam(identifier);
    }
}
