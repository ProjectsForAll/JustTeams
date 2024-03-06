package host.plas.justteams.placeholders;

import host.plas.justteams.JustTeams;
import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.managers.TeamManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class PAPIExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "teams";
    }

    @Override
    public String getAuthor() {
        return "Drak";
    }

    @Override
    public String getVersion() {
        return JustTeams.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        Optional<TeamPlayer> tagPlayerOptional = TeamManager.getPlayer(player.getUniqueId().toString());
        if (tagPlayerOptional.isEmpty()) {
            TeamManager.loadOrCreatePlayerAsync(player.getUniqueId().toString());
            return "";
        }
        TeamPlayer teamPlayer = tagPlayerOptional.get();

        if (params.equals("team")) {
            Optional<ConfiguredTeam> team = teamPlayer.getChosenTeam();
            if (team.isEmpty()) {
                return "";
            }
            return team.get().getValue();
        }
        if (params.startsWith("other_")) {
            String[] paramsSplit = params.split("_", 3);
            String other = paramsSplit[0];
            String action = paramsSplit[1];
            String name = paramsSplit[2];

            OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(name);
            if (otherPlayer == null) {
                return "";
            }

            Optional<TeamPlayer> otherTagPlayerOptional = TeamManager.getPlayer(otherPlayer.getUniqueId().toString());
            if (otherTagPlayerOptional.isEmpty()) {
                TeamManager.loadOrCreatePlayerAsync(otherPlayer.getUniqueId().toString());
                return null;
            }
            TeamPlayer otherTeamPlayer = otherTagPlayerOptional.get();

            if (action.equals("team")) {
                Optional<ConfiguredTeam> tag = otherTeamPlayer.getChosenTeam();
                if (tag.isEmpty()) {
                    return "";
                }
                return tag.get().getValue();
            }
        }

        if (params.startsWith("tag_")) {
            String[] paramsSplit = params.split("_", 2);
            String action = paramsSplit[0];
            String name = paramsSplit[1];

            Optional<ConfiguredTeam> tag = TeamManager.getTeam(name);
            if (tag.isEmpty()) {
                return "";
            }
            ConfiguredTeam configuredTeam = tag.get();

            return configuredTeam.getValue();
        }

        return null;
    }
}
