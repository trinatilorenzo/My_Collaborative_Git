package tinyswordsisland.model.event;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AudioEventQueue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient List<AudioEventType> pending = new ArrayList<>();

    public void add(AudioEventType event) {
        if (event != null) {
            pending.add(event);
        }
    }

    public List<AudioEventType> consume() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<AudioEventType> snapshot = List.copyOf(pending);
        pending.clear();
        return snapshot;
    }

    public void clear() {
        pending.clear();
    }

    public void ensureInitialized() {
        if (pending == null) {
            pending = new ArrayList<>();
        }
    }
}
