package werewolf.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PmManager {
    protected HashMap<String, PmFolder> folders;

    public PmManager(HashMap<String, PmFolder> folders) {
        this.folders = folders;
        for (String key : folders.keySet())
            folders.putIfAbsent(key.toLowerCase(), folders.get(key)); //Ensure lowercase keys are available.
    }

    public List<PmFolder> getFolders() {
        Collection<PmFolder> coll = folders.values();
        if (coll instanceof List)
            return (List<PmFolder>)coll;
        return new ArrayList<PmFolder>(coll);
    }

    public PmFolder getFolder(String name) {
        return folders.get(name.toLowerCase());
    }

    public void addFolder(String name, PmFolder folder) {
        folders.put(name.toLowerCase(), folder);
    }
}