package host.plas.justtags.data;

import host.plas.justtags.JustTags;
import host.plas.justtags.managers.TagManager;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

@Getter @Setter
public class ConfiguredTag implements Identifiable {
    private String identifier;
    private String value;

    public ConfiguredTag(String identifier, String value) {
        this.identifier = identifier;
        this.value = value;
    }

    public ConfiguredTag(String identifier) {
        this(identifier, "");
    }

    public void save(boolean async) {
        if (async) JustTags.getMainDatabase().saveTag(this);
        else JustTags.getMainDatabase().saveTag(this).join();
    }

    public void save() {
        save(true);
    }

    public void delete(boolean async) {
        if (async) JustTags.getMainDatabase().dropTag(getIdentifier());
        else JustTags.getMainDatabase().dropTag(getIdentifier()).join();

        unregister();
    }

    public void delete() {
        delete(true);
    }

    public void register() {
        TagManager.registerTag(this);
    }

    public void unregister() {
        TagManager.unregisterTag(this);
    }
}
