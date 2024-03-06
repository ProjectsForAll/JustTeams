package host.plas.justtags.data;

import host.plas.justtags.JustTags;
import host.plas.justtags.managers.TeamManager;
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
        if (async) JustTags.getMainDatabase().saveTeam(this);
        else JustTags.getMainDatabase().saveTeam(this).join();
    }

    public void save() {
        save(true);
    }

    public void delete(boolean async) {
        if (async) JustTags.getMainDatabase().dropTeam(getIdentifier());
        else JustTags.getMainDatabase().dropTeam(getIdentifier()).join();

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
