package host.plas.justteams.database;

import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.managers.TeamManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TeamsDBOperator extends DBOperator {
    public TeamsDBOperator(ConnectorSet set) {
        super(set, "JustTeams");
    }

    public void ensureDatabase() {
        String s1 = Statements.getStatement(Statements.StatementType.CREATE_DATABASE, this.getConnectorSet());
        if (s1 == null) return;
        if (s1.isBlank() || s1.isEmpty()) return;

        this.execute(s1);
    }

    public void ensureTable() {
        String s1 = Statements.getStatement(Statements.StatementType.CREATE_TABLES, this.getConnectorSet());
        if (s1 == null) return;
        if (s1.isBlank() || s1.isEmpty()) return;

        this.execute(s1);
    }

    public void ensureUsable() {
        this.ensureFile();
        this.ensureDatabase();
        this.ensureTable();
    }

    public CompletableFuture<Boolean> savePlayer(TeamPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PUSH_PLAYER_MAIN, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%uuid%", player.getIdentifier());
            s1 = s1.replace("%team%", player.getChosenTeam().map(ConfiguredTeam::getIdentifier).orElse(""));

            this.execute(s1);

            return true;
        });
    }

    public CompletableFuture<Optional<TeamPlayer>> loadPlayer(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (! playerExists(uuid).join()) return Optional.empty();

            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_PLAYER_MAIN, this.getConnectorSet());
            if (s1 == null) return Optional.empty();
            if (s1.isBlank() || s1.isEmpty()) return Optional.empty();

            s1 = s1.replace("%uuid%", uuid);

            AtomicReference<Optional<TeamPlayer>> atomicReference = new AtomicReference<>(Optional.empty());
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(Optional.empty());
                    return;
                }

                try {
                    if (set.next()) {
                        TeamPlayer player = new TeamPlayer(uuid);

                        String team = set.getString("Team");

                        if (team != null && ! team.isEmpty() && ! team.isBlank()) {
                            Optional<ConfiguredTeam> t = TeamManager.getTeam(team);
                            t.ifPresent(player::selectTeam);
                        }

                        atomicReference.set(Optional.of(player));
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                atomicReference.set(Optional.empty());
            });

            return atomicReference.get();
        });
    }

    public CompletableFuture<Boolean> playerExists(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PLAYER_EXISTS, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%uuid%", uuid);

            AtomicReference<Boolean> atomicReference = new AtomicReference<>(false);
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(false);
                    return;
                }

                try {
                    if (set.next()) {
                        int i = set.getInt(1);

                        atomicReference.set(i > 0);
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                atomicReference.set(false);
            });

            return atomicReference.get();
        });
    }

    public CompletableFuture<Boolean> saveTeam(ConfiguredTeam tag) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PUSH_TEAM, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%identifier%", tag.getIdentifier());
            s1 = s1.replace("%value%", tag.getValue());

            this.execute(s1);

            return true;
        });
    }

    public CompletableFuture<Optional<ConfiguredTeam>> loadTeam(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_TEAM, this.getConnectorSet());
            if (s1 == null) return Optional.empty();
            if (s1.isBlank() || s1.isEmpty()) return Optional.empty();

            s1 = s1.replace("%identifier%", identifier);

            AtomicReference<Optional<ConfiguredTeam>> atomicReference = new AtomicReference<>(Optional.empty());
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(Optional.empty());
                    return;
                }

                try {
                    if (set.next()) {
                        ConfiguredTeam tag = new ConfiguredTeam(identifier);

                        String value = set.getString("Value");

                        tag.setValue(value);

                        atomicReference.set(Optional.of(tag));
                    }

                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                atomicReference.set(Optional.empty());
            });

            return atomicReference.get();
        });
    }

    public CompletableFuture<Boolean> teamExists(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.TEAM_EXISTS, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%identifier%", identifier);

            AtomicReference<Boolean> atomicReference = new AtomicReference<>(false);
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(false);
                    return;
                }

                try {
                    if (set.next()) {
                        int i = set.getInt(1);

                        atomicReference.set(i > 0);
                    }

                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                atomicReference.set(false);
            });

            return atomicReference.get();
        });
    }

    public CompletableFuture<Boolean> loadAllTeams() {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_ALL_TEAMS, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            TeamManager.unregisterAllTeams();

            this.executeQuery(s1, set -> {
                if (set == null) {
                    return;
                }

                try {
                    while (set.next()) {
                        String identifier = set.getString("Identifier");
                        String value = set.getString("Value");

                        ConfiguredTeam tag = new ConfiguredTeam(identifier, value);
                        tag.register();
                    }

                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return true;
        });
    }

    public CompletableFuture<Boolean> dropTeam(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.DROP_TEAM, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%identifier%", identifier);

            this.execute(s1);

            return true;
        });
    }
}
