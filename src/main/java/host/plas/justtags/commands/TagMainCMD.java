package host.plas.justtags.commands;

import host.plas.justtags.JustTags;
import host.plas.justtags.data.ConfiguredTeam;
import host.plas.justtags.data.TeamPlayer;
import host.plas.justtags.gui.menus.EquipGui;
import host.plas.justtags.managers.TeamManager;
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

public class TagMainCMD extends SimplifiedCommand {
    public TagMainCMD() {
        super("justtags", JustTags.getInstance());
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

            new EquipGui(p1, 1).open();

            return true;
        }

        String action = commandContext.getStringArg(0).toLowerCase();

        switch (action) {
            case "reload":
                if (! sender.hasPermission("justtags.reload")) {
                    commandContext.sendMessage("&cYou do not have permission to reload the plugin!");
                    return true;
                }

                commandContext.sendMessage("&eReloading the plugin&8...");
                CompletableFuture.runAsync(() -> {
                    int size = TeamManager.getTags().size();
                    TeamManager.unregisterAllTeams();
                    commandContext.sendMessage("&cUnloaded &f" + size + " &etags&8...");
                    JustTags.getMainDatabase().loadAllTags().whenComplete((bool, throwable) -> {
                        if (throwable != null) {
                            commandContext.sendMessage("&cFailed to load tags from the database!");
                            throwable.printStackTrace();
                            return;
                        }

                        commandContext.sendMessage("&aLoaded &f" + TeamManager.getTags().size() + " &etags&8...");
                    });
                });

                TeamManager.ensurePlayers();
                break;
            case "create":
                if (! sender.hasPermission("justtags.create")) {
                    commandContext.sendMessage("&cYou do not have permission to create tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(2)) {
                    commandContext.sendMessage("&cYou must specify a tag to create!");
                    return true;
                }

                String tag = commandContext.getStringArg(1);
                if (TeamManager.isTeamLoaded(tag)) {
                    commandContext.sendMessage("&cA tag with that identifier already exists!");
                    return true;
                }

                StringBuilder value = new StringBuilder();
                for (int i = 2; i < commandContext.getArgs().size(); i++) {
                    value.append(commandContext.getStringArg(i)).append(" ");
                }
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }

                ConfiguredTeam configuredTeam = new ConfiguredTeam(tag, value.toString());
                TeamManager.registerTeam(configuredTeam);
                configuredTeam.save();

                commandContext.sendMessage("&aCreated &etag &f" + tag + "&8: " + value);
                break;
            case "delete":
                if (! sender.hasPermission("justtags.delete")) {
                    commandContext.sendMessage("&cYou do not have permission to delete tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cYou must specify a tag to delete!");
                    return true;
                }

                String tag1 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(tag1)) {
                    commandContext.sendMessage("&cA tag with that identifier does not exist!");
                    return true;
                }

                TeamManager.getTags().stream().filter(configuredTag2 -> configuredTag2.getIdentifier().equals(tag1)).findFirst().ifPresent(configuredTag3 -> {
                    TeamManager.unregisterTeam(configuredTag3);
                    configuredTag3.delete();
                });

                commandContext.sendMessage("&aDeleted &etag &f" + tag1);
                break;
            case "grant":
                if (! sender.hasPermission("justtags.grant")) {
                    commandContext.sendMessage("&cYou do not have permission to grant tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(2)) {
                    commandContext.sendMessage("&cYou must specify a player and a tag to grant!");
                    return true;
                }

                String player = commandContext.getStringArg(1);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                commandContext.sendMessage("&eChecking if the player exists&8...");
                TeamManager.loadOrCreatePlayerAsync(offlinePlayer.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to check if the player exists!");
                        throwable.printStackTrace();
                        return;
                    }

                    if (tagPlayer == null) {
                        commandContext.sendMessage("&cA player with that identifier does not exist!");
                        return;
                    }

                    String tag2 = commandContext.getStringArg(2);
                    if (! TeamManager.isTeamLoaded(tag2)) {
                        commandContext.sendMessage("&cA tag with that identifier does not exist!");
                        return;
                    }

                    tagPlayer.addAvailableTag(tag2);
                    tagPlayer.save();
                    commandContext.sendMessage("&aGranted &etag &f" + tag2 + " &ato &f" + player + "&8!");

                    tagPlayer.asPlayer().ifPresent(p -> {
                        if (p.isOnline()) {
                            MessageUtils.sendMessage(p, "&eThe tag &f" + tag2 + " &ehas been &agranted &eto you&8!");
                        }
                    });
                });
                break;
            case "revoke":
                if (! sender.hasPermission("justtags.revoke")) {
                    commandContext.sendMessage("&cYou do not have permission to revoke tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(2)) {
                    commandContext.sendMessage("&cYou must specify a player and a tag to revoke!");
                    return true;
                }

                String player1 = commandContext.getStringArg(1);
                OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(player1);
                commandContext.sendMessage("&eChecking if the player exists&8...");
                TeamManager.loadOrCreatePlayerAsync(offlinePlayer1.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to check if the player exists!");
                        throwable.printStackTrace();
                        return;
                    }

                    if (tagPlayer == null) {
                        commandContext.sendMessage("&cA player with that identifier does not exist!");
                        return;
                    }

                    String tag3 = commandContext.getStringArg(2);
                    if (! TeamManager.isTeamLoaded(tag3)) {
                        commandContext.sendMessage("&cA tag with that identifier does not exist!");
                        return;
                    }

                    tagPlayer.removeAvailableTag(tag3);
                    tagPlayer.save();
                    commandContext.sendMessage("&aRevoked &etag &f" + tag3 + " &afrom &f" + player1 + "&8!");

                    tagPlayer.asPlayer().ifPresent(p -> {
                        if (p.isOnline()) {
                            MessageUtils.sendMessage(p, "&eThe tag &f" + tag3 + " &ehas been &crevoked &efrom you&8!");
                        }
                    });
                });
                break;
            case "list":
                if (! sender.hasPermission("justtags.list")) {
                    commandContext.sendMessage("&cYou do not have permission to list tags!");
                    return true;
                }

                commandContext.sendMessage("&eListing tags&8...");
                TeamManager.getTags().forEach(configuredTag1 -> commandContext.sendMessage("&f" + configuredTag1.getIdentifier() + " &8- " + configuredTag1.getValue()));
                break;
            case "equip":
                if (! (sender instanceof Player)) {
                    commandContext.sendMessage("&cYou must be a player to use that part of the command!");
                    return true;
                }

                Player p = (Player) sender;

                if (! sender.hasPermission("justtags.equip")) {
                    commandContext.sendMessage("&cYou do not have permission to set tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
//                    commandContext.sendMessage("&cYou must specify a tag and a value to set!");
                    commandContext.sendMessage("&eOpening GUI&8...");

                    new EquipGui(p, 1).open();

                    return true;
                }

                String tag4 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(tag4)) {
                    commandContext.sendMessage("&cA tag with that identifier does not exist!");
                    return true;
                }

                Optional<Integer> index = Optional.empty();
                if (commandContext.isArgUsable(2)) {
                    Optional<Integer> optionalInteger = commandContext.getIntArg(2);
                    if (optionalInteger.isEmpty()) {
                        commandContext.sendMessage("&cYou must specify a valid index!");
                        return true;
                    }
                    index = optionalInteger;
                }

                Optional<Boolean> insert = Optional.empty();
                if (commandContext.isArgUsable(3)) {
                    Optional<Boolean> optionalBoolean = commandContext.getBooleanArg(3);
                    if (optionalBoolean.isEmpty()) {
                        commandContext.sendMessage("&cYou must specify a valid boolean value!");
                        return true;
                    }
                    insert = optionalBoolean;
                }

                Optional<Integer> finalIndex = index;
                Optional<Boolean> finalInsert = insert;
                TeamManager.loadOrCreatePlayerAsync(p.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to load or create the player!");
                        throwable.printStackTrace();
                        return;
                    }

                    if (! tagPlayer.hasAvailableTag(tag4)) {
                        commandContext.sendMessage("&cYou do not have that tag!");
                        return;
                    }

                    finalIndex.ifPresentOrElse((integer) -> {
                        int fi = integer;
                        if (fi < 0) {
                            fi = 0;
                        }
                        if (fi > TeamPlayer.getHardCapMax()) {
                            fi = TeamPlayer.getHardCapMax();
                        }

                        if (finalInsert.isPresent() && finalInsert.get()) {
                            tagPlayer.insertTag(fi, tag4);
                        } else {
                            tagPlayer.putTag(fi, tag4);
                        }
                    }, () -> tagPlayer.insertTag(0, tag4));

                    tagPlayer.save();

                    commandContext.sendMessage("&aEquipped &etag &f" + tag4 + " &ato yourself&8!");
                });
                break;
            case "unequip":
                if (! (sender instanceof Player)) {
                    commandContext.sendMessage("&cYou must be a player to use that part of the command!");
                    return true;
                }

                Player p1 = (Player) sender;

                if (! sender.hasPermission("justtags.unequip")) {
                    commandContext.sendMessage("&cYou do not have permission to set tags!");
                    return true;
                }

                if (! commandContext.isArgUsable(1)) {
//                    commandContext.sendMessage("&cYou must specify a tag to unequip!");
                    commandContext.sendMessage("&eOpening GUI&8...");

                    new EquipGui(p1, 1).open();

                    return true;
                }

                String tag5 = commandContext.getStringArg(1);
                if (! TeamManager.isTeamLoaded(tag5)) {
                    commandContext.sendMessage("&cA tag with that identifier does not exist!");
                    return true;
                }

                TeamManager.loadOrCreatePlayerAsync(p1.getUniqueId().toString()).whenComplete((tagPlayer, throwable) -> {
                    if (throwable != null) {
                        commandContext.sendMessage("&cFailed to load or create the player!");
                        throwable.printStackTrace();
                        return;
                    }

                    if (! tagPlayer.hasTag(tag5)) {
                        commandContext.sendMessage("&cYou do not have that tag equipped!");
                        return;
                    }

                    tagPlayer.removeTag(tag5);
                    tagPlayer.save();

                    commandContext.sendMessage("&aUnequipped &etag &f" + tag5 + " &afrom yourself&8!");
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
            if (sender.hasPermission("justtags.reload")) completions.add("reload");
            if (sender.hasPermission("justtags.create")) completions.add("create");
            if (sender.hasPermission("justtags.delete")) completions.add("delete");
            if (sender.hasPermission("justtags.grant")) completions.add("grant");
            if (sender.hasPermission("justtags.revoke")) completions.add("revoke");
            if (sender.hasPermission("justtags.list")) completions.add("list");
            if (sender.hasPermission("justtags.equip")) completions.add("equip");
            if (sender.hasPermission("justtags.unequip")) completions.add("unequip");
        }
        if (commandContext.getArgs().size() == 2) {
            if (commandContext.getStringArg(0).equalsIgnoreCase("grant")) {
                if (sender.hasPermission("justtags.grant")) Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("revoke")) {
                if (sender.hasPermission("justtags.revoke")) Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("equip")) {
                if (sender.hasPermission("justtags.equip")) TeamManager.getTags().forEach(configuredTag -> completions.add(configuredTag.getIdentifier()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("unequip")) {
                if (sender.hasPermission("justtags.unequip")) TeamManager.getTags().forEach(configuredTag -> completions.add(configuredTag.getIdentifier()));
            }
        }
        if (commandContext.getArgs().size() == 3) {
            if (commandContext.getStringArg(0).equalsIgnoreCase("grant")) {
                if (sender.hasPermission("justtags.grant")) TeamManager.getTags().forEach(configuredTag -> completions.add(configuredTag.getIdentifier()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("revoke")) {
                if (sender.hasPermission("justtags.revoke")) TeamManager.getTags().forEach(configuredTag -> completions.add(configuredTag.getIdentifier()));
            }
            if (commandContext.getStringArg(0).equalsIgnoreCase("equip")) {
                if (sender.hasPermission("justtags.equip")) completions.add("<index>");
            }
        }
        if (commandContext.getArgs().size() == 4) {
            if (commandContext.getStringArg(0).equalsIgnoreCase("equip")) {
                if (sender.hasPermission("justtags.equip")) completions.add("<insert?>");
                if (sender.hasPermission("justtags.equip")) completions.add("true");
                if (sender.hasPermission("justtags.equip")) completions.add("false");
            }
        }

        return completions;
    }
}
