package host.plas.justteams.gui;

import host.plas.justteams.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public abstract class TeamGui extends Gui {
    private GuiType type;

    public TeamGui(@NotNull Player player, GuiType type, int rows) {
        super(player, type.name(), getTitleByType(type), rows);

        this.type = type;
    }

    public static String getTitleByType(GuiType type) {
        String title = "";

        switch (type) {
            case TEAM_JOIN:
                title = "&3&lTeam Menu";
                break;
            default:
                break;
        }

        title = MessageUtils.colorize(title);

        return title;
    }
}
