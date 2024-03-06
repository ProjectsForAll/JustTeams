package host.plas.justtags.gui.icons;

import host.plas.justtags.data.ConfiguredTeam;
import host.plas.justtags.data.TeamPlayer;
import host.plas.justtags.managers.TeamManager;
import host.plas.justtags.utils.MessageUtils;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter @Setter
public class TagIcon extends Icon {
    private String tagIdentifier;
    private TeamPlayer player;

    public TagIcon(String tagIdentifier, TeamPlayer player) {
        super(getTagMaterial(player, tagIdentifier));

        this.tagIdentifier = tagIdentifier;
        this.player = player;

        applyEdits();
    }

    public void applyEdits() {
//        setLore(getLore());
    }

    public static boolean isEquipped(TeamPlayer player, String tagIdentifier) {
        return getIndex(player, tagIdentifier) >= 0;
    }

    public static boolean isShown(TeamPlayer player, String tagIdentifier) {
        return
                getIndex(player, tagIdentifier) <= player.getFinalMaxTags()
                        && isEquipped(player, tagIdentifier);
    }

    public static Map.Entry<Integer, ConfiguredTeam> getEntry(TeamPlayer player, String tagIdentifier) {
        Map.Entry<Integer, ConfiguredTeam> entry = null;

        for (Map.Entry<Integer, ConfiguredTeam> e : player.getContainer().entrySet()) {
            if (e.getValue().getIdentifier().equals(tagIdentifier)) {
                entry = e;
                break;
            }
        }

        return entry;
    }

    public static int getIndex(TeamPlayer player, String tagIdentifier) {
        Map.Entry<Integer, ConfiguredTeam> entry = getEntry(player, tagIdentifier);
        if (entry == null) {
            return -2; // not found -> -1 = not equipped
        }

        return entry.getKey();
    }
    
    public Optional<ConfiguredTeam> getTag() {
        return TeamManager.getTeam(getTagIdentifier());
    }

    public static BaseComponent[] getIconName(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.color("&7(" + (player.hasTag(tagIdentifier) ? "&aEquipped" : "&cUnequipped") + "&7) &bTag&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.color("&cUnknown Tag");
    }

    public static Component getIconNameComp(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.colorizeComp("&7(" + (player.hasTag(tagIdentifier) ? "&aEquipped" : "&cUnequipped") + "&7) &bTag&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.colorizeComp("&cUnknown Tag");
    }

    public static String getIconNameHard(TeamPlayer player, String tagIdentifier) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isPresent()) {
            ConfiguredTeam configuredTeam = tag.get();
            return MessageUtils.colorizeHard("&7(" + (player.hasTag(tagIdentifier) ? "&aEquipped" : "&cUnequipped") + "&7) &bTag&7: &r" + configuredTeam.getValue());
        }

        return MessageUtils.colorizeHard("&cUnknown Tag");
    }

    public static List<BaseComponent[]> getLore(TeamPlayer player, String tagIdentifier) {
        return List.of(
                MessageUtils.color("&eEquipped&7? " + (isEquipped(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.color("&eShown&7? " + (isShown(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.color("&eIndex&7: &f" + getIndex(player, tagIdentifier)),
                MessageUtils.color(""),
                MessageUtils.color("&7Click to " + (isEquipped(player, tagIdentifier) ? "&cunequip" : "&aequip") + " &7this tag.")
        );
    }

    public static List<Component> getLoreComp(TeamPlayer player, String tagIdentifier) {
        return List.of(
                MessageUtils.colorizeComp("&eEquipped&7? " + (isEquipped(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeComp("&eShown&7? " + (isShown(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeComp("&eIndex&7: &f" + getIndex(player, tagIdentifier)),
                MessageUtils.colorizeComp(""),
                MessageUtils.colorizeComp("&7Click to " + (isEquipped(player, tagIdentifier) ? "&cunequip" : "&aequip") + " &7this tag.")
        );
    }

    public static List<String> getLoreHard(TeamPlayer player, String tagIdentifier) {
        return List.of(
                MessageUtils.colorizeHard("&eEquipped&7? " + (isEquipped(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeHard("&eShown&7? " + (isShown(player, tagIdentifier) ? "&aYes" : "&cNo")),
                MessageUtils.colorizeHard("&eIndex&7: &f" + getIndex(player, tagIdentifier)),
                MessageUtils.colorizeHard(""),
                MessageUtils.colorizeHard("&7Click to " + (isEquipped(player, tagIdentifier) ? "&cunequip" : "&aequip") + " &7this tag.")
        );
    }

    public static ItemStack getTagMaterial(TeamPlayer player, String tagIdentifier) {
        ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
        if (player.hasAvailableTag(tagIdentifier)) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
//                meta.setDisplayNameComponent(getIconName(player, tagIdentifier));
//                meta.setLoreComponents(getLore(player, tagIdentifier));

//                meta.displayName(getIconNameComp(player, tagIdentifier));
//                meta.lore(getLoreComp(player, tagIdentifier));

                meta.setDisplayName(getIconNameHard(player, tagIdentifier));
                meta.setLore(getLoreHard(player, tagIdentifier));

                stack.addUnsafeEnchantment(Enchantment.MENDING, 1);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                stack.setItemMeta(meta);
            }
        }

        return stack;
    }
    
    public static Optional<TagIcon> validateAndGet(String tagIdentifier, TeamPlayer player) {
        Optional<ConfiguredTeam> tag = TeamManager.getTeam(tagIdentifier);
        if (tag.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(new TagIcon(tagIdentifier, player));
    }
}
