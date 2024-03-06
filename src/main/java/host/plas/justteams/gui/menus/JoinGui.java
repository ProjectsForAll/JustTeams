package host.plas.justteams.gui.menus;

import host.plas.justteams.data.ConfiguredTeam;
import host.plas.justteams.data.TeamPlayer;
import host.plas.justteams.gui.GuiType;
import host.plas.justteams.gui.TeamGui;
import host.plas.justteams.gui.icons.TeamIcon;
import host.plas.justteams.managers.TeamManager;
import host.plas.justteams.utils.MessageUtils;
import io.streamlined.bukkit.commands.Sender;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Optional;

@Getter @Setter
public class JoinGui extends TeamGui {
    private int currentPage;

    public JoinGui(Player player, int currentPage) {
        super(player, GuiType.TEAM_JOIN, 3);

        this.currentPage = currentPage;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        buildPage();
    }

    public void buildPage() {
        buildInner(this);

        buildNextPanel(this);
        buildPreviousPanel(this);
    }

    public static void buildInner(JoinGui gui) {
        Player player = gui.player;
        TeamPlayer teamPlayer = TeamManager.loadOrCreatePlayerAsync(player.getUniqueId().toString()).join();

        int delta = gui.getCurrentPage() * 15 - 15; // no page 0
        for (int i = 0; i < 15; i++) {
            if (delta >= TeamManager.getTeams().size()) {
                break;
            }

            Optional<String> teamIdentifier = getDeltaTeam(gui, delta);
            if (teamIdentifier.isEmpty()) {
                continue;
            }
            String team = teamIdentifier.get();

            TeamIcon icon = new TeamIcon(team, teamPlayer);

            icon.onClick(event -> {
                if (TeamIcon.isJoined(teamPlayer, icon.getTeamIdentifier())) {
                    teamPlayer.unselectTeam(icon.getTeamIdentifier());

                    teamPlayer.ifPlayer(p -> {
                        Sender sender = new Sender(p);
                        sender.sendMessage("&eYou have &cleft &ethe team &f" + icon.getTeamIdentifier() + "&e.");
                    });

                    reopen(gui);
                } else {
                    teamPlayer.selectTeam(icon.getTeamIdentifier());

                    teamPlayer.ifPlayer(p -> {
                        Sender sender = new Sender(p);
                        sender.sendMessage("&eYou have &ajoined &ethe team &f" + icon.getTeamIdentifier() + "&e.");
                    });

                    reopen(gui);
                }
            });

            gui.addItem(getGuiSlot(i), icon);

            delta ++;
        }
    }

    public static void reopen(JoinGui gui) {
        new JoinGui(gui.player, gui.getCurrentPage()).open();
    }

    public static Optional<String> getDeltaTeam(JoinGui gui, int delta) {
        int i = 0;
        for (ConfiguredTeam team : TeamManager.getTeams()) {
            if (i == delta) {
                return Optional.of(team.getIdentifier());
            }
            i++;
        }

        return Optional.empty();
    }

    public static int getGuiSlot(int deltaIndex) {
        if (deltaIndex < 5) {
            return deltaIndex + 2;
        }
        if (deltaIndex < 10) {
            return deltaIndex + 6;
        }
        return deltaIndex + 10;
    }

    public static void buildPreviousPanel(JoinGui gui) {
        gui.addItem(0, getAir());
        gui.addItem(1, getAir());
//        addItem(9, getAir()); // is the button
        gui.addItem(10, getAir());
        gui.addItem(18, getAir());
        gui.addItem(19, getAir());

        gui.addItem(9, getPreviousButton(gui));
    }

    public static Icon getAir() {
        return new Icon(Material.AIR);
    }

    public static Icon getPreviousButton(JoinGui gui) {
        Icon icon = new Icon(Material.ARROW);
        icon.setName(MessageUtils.colorize("&3&l<<< &bPrevious Page"));

        icon.onClick(event -> {
            paginate(gui, -1);
        });

        return icon;
    }

    public static void buildNextPanel(JoinGui gui) {
        gui.addItem(7, getAir());
        gui.addItem(8, getAir());
        gui.addItem(16, getAir());
//        gui.addItem(17, getAir()); // is the button
        gui.addItem(25, getAir());
        gui.addItem(26, getAir());

        gui.addItem(17, getNextButton(gui));
    }

    public static Icon getNextButton(JoinGui gui) {
        Icon icon = new Icon(Material.ARROW);
        icon.setName(MessageUtils.colorize("&3&lNext Page &b>>>"));

        icon.onClick(event -> {
            paginate(gui, 1);
        });

        return icon;
    }

    public static void paginate(JoinGui gui, int amount) {
        int current = gui.getCurrentPage();
        int newPage = getNewPage(gui, amount);
        if (newPage == current) {
            return;
        }
        new JoinGui(gui.player, newPage).open();
    }

    public static int getNewPage(JoinGui gui, int amount) {
        int page = gui.getCurrentPage();
        page += amount;

        if (page > getMaxPages(gui.player)) {
            page = 1;
        }

        if (page < 1) {
            page = getMaxPages(gui.player);
        }

        return page;
    }

    public static int getMaxPages(Player player) {
        int totalTeams = TeamManager.getTeams().size();

        // if there are 15 or less tags, there is only 1 page
        // if there are 30 or less tags, there are 2 pages
        // if there are 33 tags, there are 3 pages
        int r = (int) Math.ceil((double) totalTeams / 15);
        if (r <= 0) {
            r = 1;
        }
        return r;
    }
}
