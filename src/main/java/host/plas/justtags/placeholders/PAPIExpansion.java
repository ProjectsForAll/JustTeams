package host.plas.justtags.placeholders;

import host.plas.justtags.JustTags;
import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import host.plas.justtags.managers.TagManager;
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
        Optional<TagPlayer> tagPlayerOptional = TagManager.getPlayer(player.getUniqueId().toString());
        if (tagPlayerOptional.isEmpty()) {
            TagManager.loadOrCreatePlayerAsync(player.getUniqueId().toString());
            return "";
        }
        TagPlayer tagPlayer = tagPlayerOptional.get();

        if (params.equals("container")) {
            return tagPlayer.getContainerString();
        }
        if (params.startsWith("i")) {
            int index = Integer.parseInt(params.substring(1));
            Optional<ConfiguredTag> tag = tagPlayer.getTag(index);
            if (tag.isEmpty()) {
                return "";
            }
            ConfiguredTag configuredTag = tag.get();
            return configuredTag.getValue();
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

            Optional<TagPlayer> otherTagPlayerOptional = TagManager.getPlayer(otherPlayer.getUniqueId().toString());
            if (otherTagPlayerOptional.isEmpty()) {
                TagManager.loadOrCreatePlayerAsync(otherPlayer.getUniqueId().toString());
                return null;
            }
            TagPlayer otherTagPlayer = otherTagPlayerOptional.get();

            if (action.equals("container")) {
                return otherTagPlayer.getContainerString();
            }
            if (action.startsWith("i")) {
                int index = Integer.parseInt(action.substring(1));
                Optional<ConfiguredTag> tag = otherTagPlayer.getTag(index);
                if (tag.isEmpty()) {
                    return "";
                }
                ConfiguredTag configuredTag = tag.get();
                return configuredTag.getValue();
            }
        }
        if (params.equals("max")) {
            return String.valueOf(tagPlayer.getFinalMaxTags());
        }

        if (params.startsWith("tag_")) {
            String[] paramsSplit = params.split("_", 2);
            String action = paramsSplit[0];
            String name = paramsSplit[1];

            Optional<ConfiguredTag> tag = TagManager.getTag(name);
            if (tag.isEmpty()) {
                return "";
            }
            ConfiguredTag configuredTag = tag.get();

            return configuredTag.getValue();
        }

        return null;
    }
}
