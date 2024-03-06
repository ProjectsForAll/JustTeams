package host.plas.justteams.commands;

import host.plas.justteams.JustTeams;
import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.gui.menus.JoinGui;
import host.plas.justteams.managers.TeamManager;
import io.streamlined.bukkit.MessageUtils;
import io.streamlined.bukkit.commands.CommandContext;
import io.streamlined.bukkit.commands.SimplifiedCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class TeamMainCMD extends SimplifiedCommand {
    public TeamMainCMD() {
        super("justteams", JustTeams.getInstance());
    }

    @Override
    public boolean command(CommandContext commandContext) {
        CommandSender sender = commandContext.getSender().getCommandSender().orElse(null);
        if (sender == null) {
            commandContext.sendMessage("&cCould not find you as a sender!");
            return false;
        }

        if (! commandContext.isArgUsable(0)) {
//            commandContext.sendMessage("&cYou must specify an action!");
            commandContext.sendMessage("&eOpening GUI&8...");

            if (! (sender instanceof Player)) {
                commandContext.sendMessage("&cYou must be a player to use that part of the command!");
                return true;
            }

            Player p1 = (Player) sender;

            new JoinGui(p1, 1).open();

            return true;
        }

        String action = commandContext.getStringArg(0).toLowerCase();

        switch (action) {
            case "reload":
                if (! sender.hasPermission("justteams.reload")) {
                    commandContext.sendMessage("&cYou do not have permission to reload the plugin!");
                    return true;
                }

                commandContext.sendMessage("&eReloading the plugin&8...");
                CompletableFuture.runAsync(() -> {
                    int size = TeamManager.getTeams().size();
                    TeamManager.unregisterAllTeams();
                    commandContext.sendMessage("&cUnloaded &f" + size + " &eteams&8...");
                    JustTeams.getMainDatabase().loadAllTeams().whenComplete((bool, throwable) -> {
                        if (throwable != null) {
                            commandContext.sendMessage("&cFailed to load teams from the database!");
                            throwable.printStackTrace();
                            return;
                        }

                        commandContext.sendMessage("&aLoaded &f" + TeamManager.getTeams().size() + " &eteams&8...");
                    });
                });

                TeamManager.ensurePlayers();
                break;
            case "create":
                if (! sender.hasPermission("justteams.create")) {
                    commandContext.sendMessage("&cYou do not have permission to create teams!");
                    return true;
                }

                if (! commandContext.isArgUsable(2)) {
                    commandContext.sendMessage("&cYou must specify a team to create!");
                    return true;
                }

                String team = commandContext.getStringArg(1);
                if (TeamManager.isTeamLoaded(team)) {
                    commandContext.sendMessage("&cA team with that identifier already exists!");
                    return true;
                }

                StringBuilder value = new StringBuilder();
                for (int i = 2; i < commandContext.getArgs().size(); i++) {
                    value.append(commandContext.getStringArg(i)).append(" ");
                }
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }

                ConfiguredTeam configuredTeam = new ConfiguredTeam(team, value.toString());
                TeamManager.registerTeam(configuredTeam);
                configuredTeam.save();

                commandContext.sendMessage("&aCreated &eteam &f" + team + "&8: " + value);
                break;
            case "delete":
                if (! sender.hasPermission("justteams.delete")) {
                    commandContext.sendMessage("&cYou do not have permission to delete teams!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cYou must specify a team to delete!");
                    return true;
                }

                String team1 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(team1)) {
                    commandContext.sendMessage("&cA team with that identifier does not exist!");
                    return true;
                }

                TeamManager.getTeams().stream().filter(configuredteam2 -> configuredteam2.getIdentifier().equals(team1)).findFirst().ifPresent(configuredteam3 -> {
                    TeamManager.unregisterTeam(configuredteam3);
                    configuredteam3.delete();
                });

                commandContext.sendMessage("&aDeleted &eteam &f" + team1);
                break;
//            case "grant":
//                if (! sender.hasPermission("justteams.grant")) {
//                    commandContext.sendMessage("&cYou do not have permission to grant teams!");
//                    return true;
//                }
//
//                if (! commandContext.isArgUsable(2)) {
//                    commandContext.sendMessage("&cYou must specify a player and a team to grant!");
//                    return true;
//                }
//
//                String player = commandContext.getStringArg(1);
//                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
//                commandContext.sendMessage("&eChecking if the player exists&8...");
//                TeamManager.loadOrCreatePlayerAsync(offlinePlayer.getUniqueId().toString()).whenComplete((teamPlayer, throwable) -> {
//                    if (throwable != null) {
//                        commandContext.sendMessage("&cFailed to check if the player exists!");
//                        throwable.printStackTrace();
//                        return;
//                    }
//
//                    if (teamPlayer == null) {
//                        commandContext.sendMessage("&cA player with that identifier does not exist!");
//                        return;
//                    }
//
//                    String team2 = commandContext.getStringArg(2);
//                    if (! TeamManager.isTeamLoaded(team2)) {
//                        commandContext.sendMessage("&cA team with that identifier does not exist!");
//                        return;
//                    }
//
//                    teamPlayer.addAvailableteam(team2);
//                    teamPlayer.save();
//                    commandContext.sendMessage("&aGranted &eteam &f" + team2 + " &ato &f" + player + "&8!");
//
//                    teamPlayer.asPlayer().ifPresent(p -> {
//                        if (p.isOnline()) {
//                            MessageUtils.sendMessage(p, "&eThe team &f" + team2 + " &ehas been &agranted &eto you&8!");
//                        }
//                    });
//                });
//                break;
//            case "revoke":
//                if (! sender.hasPermission("justteams.revoke")) {
//                    commandContext.sendMessage("&cYou do not have permission to revoke teams!");
//                    return true;
//                }
//
//                if (! commandContext.isArgUsable(2)) {
//                    commandContext.sendMessage("&cYou must specify a player and a team to revoke!");
//                    return true;
//                }
//
//                String player1 = commandContext.getStringArg(1);
//                OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(player1);
//                commandContext.sendMessage("&eChecking if the player exists&8...");
//                TeamManager.loadOrCreatePlayerAsync(offlinePlayer1.getUniqueId().toString()).whenComplete((teamPlayer, throwable) -> {
//                    if (throwable != null) {
//                        commandContext.sendMessage("&cFailed to check if the player exists!");
//                        throwable.printStackTrace();
//                        return;
//                    }
//
//                    if (teamPlayer == null) {
//                        commandContext.sendMessage("&cA player with that identifier does not exist!");
//                        return;
//                    }
//
//                    String team3 = commandContext.getStringArg(2);
//                    if (! TeamManager.isTeamLoaded(team3)) {
//                        commandContext.sendMessage("&cA team with that identifier does not exist!");
//                        return;
//                    }
//
//                    teamPlayer.removeAvailableteam(team3);
//                    teamPlayer.save();
//                    commandContext.sendMessage("&aRevoked &eteam &f" + team3 + " &afrom &f" + player1 + "&8!");
//
//                    teamPlayer.asPlayer().ifPresent(p -> {
//                        if (p.isOnline()) {
//                            MessageUtils.sendMessage(p, "&eThe team &f" + team3 + " &ehas been &crevoked &efrom you&8!");
//                        }
//                    });
//                });
//                break;
            case "list":
                if (! sender.hasPermission("justteams.list")) {
                    commandContext.sendMessage("&cYou do not have permission to list teams!");
                    return true;
                }

                commandContext.sendMessage("&eListing teams&8...");
                TeamManager.getTeams().forEach(configuredteam1 -> commandContext.sendMessage("&f" + configuredteam1.getIdentifier() + " &8- " + configuredteam1.getValue()));
                break;
            case "join":
                if (! (sender instanceof Player)) {
                    commandContext.sendMessage("&cYou must be a player to use that part of the command!");
                    return true;
                }

                Player p = (Player) sender;

                if (! sender.hasPermission("justteams.join")) {
                    commandContext.sendMessage("&cYou do not have permission to set teams!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
//                    commandContext.sendMessage("&cYou must specify a team and a value to set!");
                    commandContext.sendMessage("&eOpening GUI&8...");

                    new JoinGui(p, 1).open();

                    return true;
                }

                String team4 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(team4)) {
                    commandContext.sendMessage("&cA team with that identifier does not exist!");
                    return true;
                }
                
                TeamManager.loadOrCreatePlayerAsync(p.getUniqueId().toString()).whenComplete((teamPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to load or create the player!");
                        throwable.printStackTrace();
                        return;
                    }

//                    if (! teamPlayer.hasAvailableteam(team4)) {
//                        commandContext.sendMessage("&cYou do not have that team!");
//                        return;
//                    }

                    teamPlayer.selectTeam(team4);

                    teamPlayer.save();

                    commandContext.sendMessage("&aJoined &eteam &f" + team4 + " &ato yourself&8!");
                });
                break;
            case "leave":
                if (! (sender instanceof Player)) {
                    commandContext.sendMessage("&cYou must be a player to use that part of the command!");
                    return true;
                }

                Player p1 = (Player) sender;

                if (! sender.hasPermission("justteams.leave")) {
                    commandContext.sendMessage("&cYou do not have permission to set teams!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
//                    commandContext.sendMessage("&cYou must specify a team to leave!");
                    commandContext.sendMessage("&eOpening GUI&8...");

                    new JoinGui(p1, 1).open();

                    return true;
                }

                String team5 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(team5)) {
                    commandContext.sendMessage("&cA team with that identifier does not exist!");
                    return true;
                }

                TeamManager.loadOrCreatePlayerAsync(p1.getUniqueId().toString()).whenComplete((teamPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to load or create the player!");
                        throwable.printStackTrace();
                        return;
                    }

                    if (! teamPlayer.hasTeam(team5)) {
                        commandContext.sendMessage("&cYou do not have that team joined!");
                        return;
                    }

                    teamPlayer.unselectTeam(team5);
                    teamPlayer.save();

                    commandContext.sendMessage("&aLeft &eteam &f" + team5 + " &afrom yourself&8!");
                });
                break;
            default:
                commandContext.sendMessage("&cInvalid action!");
                break;
        }

        return false;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext commandContext) {
        CommandSender sender = commandContext.getSender().getCommandSender().orElse(null);
        if (sender == null) return new ConcurrentSkipListSet<>();

        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();

        if (commandContext.getArgs().size() == 1) {
            if (sender.hasPermission("justteams.reload")) completions.add("reload");
            if (sender.hasPermission("justteams.create")) completions.add("create");
            if (sender.hasPermission("justteams.delete")) completions.add("delete");
            if (sender.hasPermission("justteams.grant")) completions.add("grant");
            if (sender.hasPermission("justteams.revoke")) completions.add("revoke");
            if (sender.hasPermission("justteams.list")) completions.add("list");
            if (sender.hasPermission("justteams.join")) completions.add("join");
            if (sender.hasPermission("justteams.leave")) completions.add("leave");
        }
        if (commandContext.getArgs().size() == 2) {
            if (commandContext.getStringArg(0).equalsIgnoreCase("grant")) {
                if (sender.hasPermission("justteams.grant")) Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("revoke")) {
                if (sender.hasPermission("justteams.revoke")) Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("join")) {
                if (sender.hasPermission("justteams.join")) TeamManager.getTeams().forEach(configuredteam -> completions.add(configuredteam.getIdentifier()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("leave")) {
                if (sender.hasPermission("justteams.leave")) TeamManager.getTeams().forEach(configuredteam -> completions.add(configuredteam.getIdentifier()));
            }
        }
        if (commandContext.getArgs().size() == 3) {
            if (commandContext.getStringArg(0).equalsIgnoreCase("grant")) {
                if (sender.hasPermission("justteams.grant")) TeamManager.getTeams().forEach(configuredteam -> completions.add(configuredteam.getIdentifier()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("revoke")) {
                if (sender.hasPermission("justteams.revoke")) TeamManager.getTeams().forEach(configuredteam -> completions.add(configuredteam.getIdentifier()));
            }
        }

        return completions;
    }
}
