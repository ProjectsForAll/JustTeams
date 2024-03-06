package host.plas.justtags.placeholders;

import host.plas.justtags.JustTags;
import host.plas.justtags.data.ConfiguredTeam;
import host.plas.justtags.data.TeamPlayer;
import host.plas.justtags.managers.TeamManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class PAPIExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "tags";
    }

    @Override
    public String getAuthor() {
        return "Drak";
    }

    @Override
    public String getVersion() {
        return JustTags.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        Optional<TeamPlayer> tagPlayerOptional = TeamManager.getPlayer(player.getUniqueId().toString());
        if (tagPlayerOptional.isEmpty()) {
            TeamManager.loadOrCreatePlayerAsync(player.getUniqueId().toString());
            return "";
        }
        TeamPlayer teamPlayer = tagPlayerOptional.get();

        if (params.equals("container")) {
            return teamPlayer.getContainerString();
        }
        if (params.startsWith("i")) {
            int index = Integer.parseInt(params.substring(1));
            Optional<ConfiguredTeam> tag = teamPlayer.getTag(index);
            if (tag.isEmpty()) {
                return "";
            }
            ConfiguredTeam configuredTeam = tag.get();
            return configuredTeam.getValue();
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

            if (action.equals("container")) {
                return otherTeamPlayer.getContainerString();
            }
            if (action.startsWith("i")) {
                int index = Integer.parseInt(action.substring(1));
                Optional<ConfiguredTeam> tag = otherTeamPlayer.getTag(index);
                if (tag.isEmpty()) {
                    return "";
                }
                ConfiguredTeam configuredTeam = tag.get();
                return configuredTeam.getValue();
            }
        }
        if (params.equals("max")) {
            return String.valueOf(teamPlayer.getFinalMaxTags());
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
