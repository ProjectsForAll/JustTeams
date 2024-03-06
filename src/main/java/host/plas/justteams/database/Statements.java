package host.plas.justteams.database;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Statements {
    @Getter
    public enum MySQL {
        CREATE_DATABASE("CREATE DATABASE IF NOT EXISTS `%database%`;"),
        CREATE_TABLES(
                "CREATE TABLE IF NOT EXISTS `%table_prefix%players` (" +
                "Uuid VARCHAR(36) NOT NULL," +
                "Team TEXT NOT NULL," +
                "PRIMARY KEY (Uuid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%teams` (" +
                "Identifier VARCHAR(36) NOT NULL," +
                "Value TEXT NOT NULL," +
                "PRIMARY KEY (Identifier)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        ),
        PUSH_PLAYER_MAIN("INSERT INTO `%table_prefix%players` (" +
                "Uuid, Team" +
                ") VALUES (" +
                "'%uuid%', '%team%'" +
                ") ON DUPLICATE KEY UPDATE " +
                "Team = '%team%'" +
                ";"),
        PUSH_TEAM("INSERT INTO `%table_prefix%teams` (" +
                "Identifier, Value" +
                ") VALUES (" +
                "'%identifier%', '%value%'" +
                ") ON DUPLICATE KEY UPDATE " +
                "Value = '%value%'" +
                ";"),
        PULL_PLAYER_MAIN("SELECT * FROM `%table_prefix%players` WHERE Uuid = '%uuid%';"),
        PULL_TEAM("SELECT * FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        PULL_ALL_TEAMS("SELECT * FROM `%table_prefix%teams`;"),
        DROP_TEAM("DELETE FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        PLAYER_EXISTS("SELECT COUNT(*) FROM `%table_prefix%players` WHERE Uuid = '%uuid%';"),
        TEAM_EXISTS("SELECT COUNT(*) FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        ;

        private final String statement;

        MySQL(String statement) {
            this.statement = statement;
        }
    }

    @Getter
    public enum SQLite {
        CREATE_DATABASE(""),
        CREATE_TABLES(
                "CREATE TABLE IF NOT EXISTS `%table_prefix%players` (" +
                "Uuid TEXT NOT NULL," +
                "Team TEXT NOT NULL," +
                "PRIMARY KEY (Uuid)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%teams` (" +
                "Identifier TEXT NOT NULL," +
                "Value TEXT NOT NULL," +
                "PRIMARY KEY (Identifier)" +
                ");"
        ),
        PUSH_PLAYER_MAIN("INSERT OR REPLACE INTO `%table_prefix%players` (" +
                "Uuid, Team" +
                ") VALUES (" +
                "'%uuid%', '%team%'" +
                ");"),
        PUSH_TEAM("INSERT OR REPLACE INTO `%table_prefix%teams` (" +
                "Identifier, Value" +
                ") VALUES (" +
                "'%identifier%', '%value%'" +
                ");"),
        PULL_PLAYER_MAIN("SELECT * FROM `%table_prefix%players` WHERE Uuid = '%uuid%';"),
        PULL_TEAM("SELECT * FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        PULL_ALL_TEAMS("SELECT * FROM `%table_prefix%teams`;"),
        DROP_TEAM("DELETE FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        PLAYER_EXISTS("SELECT COUNT(*) FROM `%table_prefix%players` WHERE Uuid = '%uuid%';"),
        TEAM_EXISTS("SELECT COUNT(*) FROM `%table_prefix%teams` WHERE Identifier = '%identifier%';"),
        ;

        private final String statement;

        SQLite(String statement) {
            this.statement = statement;
        }
    }

    public enum StatementType {
        CREATE_DATABASE,
        CREATE_TABLES,
        PUSH_PLAYER_MAIN,
        PUSH_TEAM,
        PULL_PLAYER_MAIN,
        PULL_TEAM,
        PULL_ALL_TEAMS,
        DROP_TEAM,
        PLAYER_EXISTS,
        TEAM_EXISTS,
        ;
    }

    public static String getStatement(StatementType type, ConnectorSet connectorSet) {
        switch (connectorSet.getType()) {
            case MYSQL:
                return MySQL.valueOf(type.name()).getStatement()
                        .replace("%database%", connectorSet.getDatabase())
                        .replace("%table_prefix%", connectorSet.getTablePrefix());
            case SQLITE:
                return SQLite.valueOf(type.name()).getStatement()
                        .replace("%table_prefix%", connectorSet.getTablePrefix());
            default:
                return "";
        }
    }
}
