package host.plas.justteams.gui.icons;

import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.managers.TeamManager;
import host.plas.justteams.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter @Setter
public class TeamIcon extends Icon {
    private String teamIdentifier;
    private TeamPlayer player;

    public TeamIcon(String teamIdentifier, TeamPlayer player) {
        super(getTeamMaterial(player, teamIdentifier));

        this.teamIdentifier = teamIdentifier;
        this.player = player;
    }

    public Optional<ConfiguredTeam> getTeam() {
        return getTeam(getTeamIdentifier());
    }

    public static Optional<ConfiguredTeam> getTeam(String teamIdentifier) {
        return TeamManager.getTeam(teamIdentifier);
    }

    public static boolean isJoined(TeamPlayer player, String tagIdentifier) {
        return player.hasTeam(tagIdentifier);
    }

    public static BaseComponent[] getIconName(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.color("&7(" + (isJoined(player, tagIdentifier) ? "&aEquipped" : "&cUnequipped") + "&7) &bTeam&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.color("&cUnknown Tag");
    }

    public static Component getIconNameComp(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.colorizeComp("&7(" + (isJoined(player, tagIdentifier) ? "&aEquipped" : "&cUnequipped") + "&7) &bTeam&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.colorizeComp("&cUnknown Tag");
    }

    public static String getIconNameHard(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.colorizeHard("&7(" + (isJoined(player, tagIdentifier) ? "&aJoined" : "&cNot Joined") + "&7) &bTeam&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.colorizeHard("&cUnknown Tag");
    }

    public static List<BaseComponent[]> getLore(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> configuredTeam = getTeam(tagIdentifier);
        if (configuredTeam.isEmpty()) {
            BaseComponent[] l = MessageUtils.color("&cUnknown Tag");
            List<BaseComponent[]> lore = new ArrayList<>();
            lore.add(l);
            return lore;
        }
        ConfiguredTeam team = configuredTeam.get();

        return List.of(
                MessageUtils.color("&eJoined&7? " + (isJoined(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.color("&eName&7: &f" + team.getValue()),
                MessageUtils.color(""),
                MessageUtils.color("&7Click to " + (isJoined(player, tagIdentifier) ? "&cleave" : "&ajoin") + " &7this tag.")
        );
    }

    public static List<Component> getLoreComp(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> configuredTeam = getTeam(tagIdentifier);
        if (configuredTeam.isEmpty()) {
            return List.of(
                    MessageUtils.colorizeComp("&cUnknown Tag")
            );
        }
        ConfiguredTeam team = configuredTeam.get();

        return List.of(
                MessageUtils.colorizeComp("&eJoined&7? " + (isJoined(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeComp("&eName&7: &f" + team.getValue()),
                MessageUtils.colorizeComp(""),
                MessageUtils.colorizeComp("&7Click to " + (isJoined(player, tagIdentifier) ? "&cleave" : "&ajoin") + " &7this tag.")
        );
    }

    public static List<String> getLoreHard(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> configuredTeam = getTeam(tagIdentifier);
        if (configuredTeam.isEmpty()) {
            return List.of(
                    MessageUtils.colorizeHard("&cUnknown Tag")
            );
        }
        ConfiguredTeam team = configuredTeam.get();

        return List.of(
                MessageUtils.colorizeHard("&eJoined&7? " + (isJoined(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeHard("&eName&7: &f" + team.getValue()),
                MessageUtils.colorizeHard(""),
                MessageUtils.colorizeHard("&7Click to " + (isJoined(player, tagIdentifier) ? "&cleave" : "&ajoin") + " &7this tag.")
        );
    }

    public static ItemStack getTeamMaterial(TeamPlayer player, String tagIdentifier) {
        ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
//                meta.setDisplayNameComponent(getIconName(player, tagIdentifier));
//                meta.setLoreComponents(getLore(player, tagIdentifier));

//                meta.displayName(getIconNameComp(player, tagIdentifier));
//                meta.lore(getLoreComp(player, tagIdentifier));

            meta.setDisplayName(getIconNameHard(player, tagIdentifier));
            meta.setLore(getLoreHard(player, tagIdentifier));

            if (isJoined(player, tagIdentifier)) stack.addUnsafeEnchantment(Enchantment.MENDING, 1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            stack.setItemMeta(meta);
        }

        return stack;
    }
    
    public static Optional<TeamIcon> validateAndGet(String tagIdentifier, TeamPlayer player) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(new TeamIcon(tagIdentifier, player));
    }
}
