package host.plas.justtags.database;

import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import host.plas.justtags.managers.TagManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TagsDBOperator extends DBOperator {
    public TagsDBOperator(ConnectorSet set) {
        super(set, "Pacifism");
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

    public CompletableFuture<Boolean> savePlayer(TagPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PUSH_PLAYER_MAIN, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%uuid%", player.getIdentifier());
            s1 = s1.replace("%container%", player.getComputableContainer());
            s1 = s1.replace("%available%", player.getComputableAvailableTags());

            this.execute(s1);

            return true;
        });
    }

    public CompletableFuture<Optional<TagPlayer>> loadPlayer(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (! playerExists(uuid).join()) return Optional.empty();

            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_PLAYER_MAIN, this.getConnectorSet());
            if (s1 == null) return Optional.empty();
            if (s1.isBlank() || s1.isEmpty()) return Optional.empty();

            s1 = s1.replace("%uuid%", uuid);

            AtomicReference<Optional<TagPlayer>> atomicReference = new AtomicReference<>(Optional.empty());
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(Optional.empty());
                    return;
                }

                try {
                    if (set.next()) {
                        TagPlayer player = new TagPlayer(uuid);

                        String container = set.getString("Container");
                        String available = set.getString("Available");

                        player.computeContainer(container);
                        player.computeAvailableTags(available);

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

    public CompletableFuture<Boolean> saveTag(ConfiguredTag tag) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PUSH_TAG, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%identifier%", tag.getIdentifier());
            s1 = s1.replace("%value%", tag.getValue());

            this.execute(s1);

            return true;
        });
    }

    public CompletableFuture<Optional<ConfiguredTag>> loadTag(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_TAG, this.getConnectorSet());
            if (s1 == null) return Optional.empty();
            if (s1.isBlank() || s1.isEmpty()) return Optional.empty();

            s1 = s1.replace("%identifier%", identifier);

            AtomicReference<Optional<ConfiguredTag>> atomicReference = new AtomicReference<>(Optional.empty());
            this.executeQuery(s1, set -> {
                if (set == null) {
                    atomicReference.set(Optional.empty());
                    return;
                }

                try {
                    if (set.next()) {
                        ConfiguredTag tag = new ConfiguredTag(identifier);

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

    public CompletableFuture<Boolean> tagExists(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.TAG_EXISTS, this.getConnectorSet());
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

    public CompletableFuture<Boolean> loadAllTags() {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.PULL_ALL_TAGS, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            TagManager.unregisterAllTags();

            this.executeQuery(s1, set -> {
                if (set == null) {
                    return;
                }

                try {
                    while (set.next()) {
                        String identifier = set.getString("Identifier");
                        String value = set.getString("Value");

                        ConfiguredTag tag = new ConfiguredTag(identifier, value);
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

    public CompletableFuture<Boolean> dropTag(String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(Statements.StatementType.DROP_TAG, this.getConnectorSet());
            if (s1 == null) return false;
            if (s1.isBlank() || s1.isEmpty()) return false;

            s1 = s1.replace("%identifier%", identifier);

            this.execute(s1);

            return true;
        });
    }
}
