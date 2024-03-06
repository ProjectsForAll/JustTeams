package host.plas.justtags.gui;

import host.plas.justtags.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public abstract class TagGui extends Gui {
    private GuiType type;

    public TagGui(@NotNull Player player, GuiType type, int rows) {
        super(player, type.name(), getTitleByType(type), rows);

        this.type = type;
    }

    public static String getTitleByType(GuiType type) {
        String title = "";

        switch (type) {
            case TAG_EQUIP:
                title = "&c&lEquip Menu";
                break;
            default:
                break;
        }

        title = MessageUtils.colorize(title);

        return title;
    }
}
