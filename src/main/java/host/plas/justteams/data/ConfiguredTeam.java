package host.plas.justteams.data;

import host.plas.justteams.JustTeams;
import host.plas.justteams.managers.TeamManager;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

@Getter @Setter
public class ConfiguredTeam implements Identifiable {
    private String identifier;
    private String value;

    public ConfiguredTeam(String identifier, String value) {
        this.identifier = identifier;
        this.value = value;
    }

    public ConfiguredTeam(String identifier) {
        this(identifier, "");
    }

    public void save(boolean async) {
        if (async) JustTeams.getMainDatabase().saveTeam(this);
        else JustTeams.getMainDatabase().saveTeam(this).join();
    }

    public void save() {
        save(true);
    }

    public void delete(boolean async) {
        if (async) JustTeams.getMainDatabase().dropTeam(getIdentifier());
        else JustTeams.getMainDatabase().dropTeam(getIdentifier()).join();

        unregister();
    }

    public void delete() {
        delete(true);
    }

    public void register() {
        TeamManager.registerTeam(this);
    }

    public void unregister() {
        TeamManager.unregisterTeam(this);
    }
}
