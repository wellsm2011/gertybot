package werewolf.net;

import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public abstract class PmFolder {
    private static class LinkedPmList implements Iterable<PrivateMessage> {
        TreeMap<Integer, PrivateMessage> tree = new TreeMap<>();
        PrivateMessage                   currentElement = null;

        public boolean contains(PrivateMessage o) {
            return tree.containsValue(o);
        }


        @Override
        public Iterator<PrivateMessage> iterator() {
            return tree.values().iterator();
        }


        public boolean add(PrivateMessage e) {
            return tree.putIfAbsent(-e.getId(), e) != null;
        }


        public boolean addAll(Collection<? extends PrivateMessage> c) {
            boolean ret = false;
            for (PrivateMessage msg : c)
                ret = ret || add(msg);
            return ret;
        }

        public boolean removeAll(Collection<? extends PrivateMessage> c) {
            boolean ret = false;
            for (PrivateMessage obj : c) {
                ret = ret || remove(obj);
            }
            return ret;
        }


        public PrivateMessage get(int index) {
            for (PrivateMessage msg : tree.values()) {
                if (index-- == 0)
                    return msg;
            }
            return null;
        }


        public PrivateMessage getNext(PrivateMessage previous) {
            boolean returnNext = false;
            for (PrivateMessage msg : tree.values()) {
                if (returnNext == true)
                    return msg;
                if (msg.equals(previous))
                    returnNext = true;
            }
            return null;
        }

        public PrivateMessage getNext() {
            if (currentElement == null)
                currentElement = get(0);
            else
                currentElement = getNext(currentElement);
            return currentElement;
        }

        public PrivateMessage getPrevious(PrivateMessage next) {
            PrivateMessage ret = null;
            for (PrivateMessage msg : tree.values()) {
                if (msg.equals(next))
                    return ret;
                ret = msg;
            }
            return null;
        }

        public PrivateMessage getPrevious() {
            if (currentElement == null)
                currentElement = get(size() - 1);
            else
                currentElement = getPrevious(currentElement);
            return currentElement;
        }

        public boolean hasNext() {
            return getCurrentIndex() != size() - 1;
        }

        public boolean hasPrevious() {
            for (PrivateMessage msg : tree.values()) {
                if (!msg.equals(currentElement))
                    return currentElement != null;
                break;
            }
            return false;
        }

        public int getCurrentIndex() {
            return indexOf(currentElement);
        }


        public int indexOf(PrivateMessage o) {
            int index = 0;
            for (PrivateMessage msg : tree.values()) {
                if (msg.equals(o))
                    return index;
                index++;
            }
            return -1;
        }


        public int size() {
            return tree.size();
        }


        public boolean isEmpty() {
            return tree.isEmpty();
        }


        public boolean remove(PrivateMessage msg) {
            if (msg.equals(currentElement))
                currentElement = getPrevious(currentElement);
            return tree.remove(-msg.getId()) != null;
        }


        public void clear() {
            tree.clear();
            currentElement = null;
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof LinkedPmList)
                return tree.equals(((LinkedPmList)o).tree);
            return false;
        }


        @Override
        public int hashCode() {
            return tree.hashCode();
        }
    }

    private LinkedPmList   pms = new LinkedPmList();
    private boolean        completeLoad = false;
    protected final String folderName;

    public PmFolder(String folderName) {
        this.folderName = folderName;
    }

    public void deleteMessage(PrivateMessage msg) throws IOException, IllegalArgumentException {
        if (!pms.contains(msg))
            throw new IllegalArgumentException("Message not found in folder.");
        deleteMessageFromFolder(msg.getId());
        pms.remove(msg);
    }

    public PrivateMessage getNextPm() throws IOException {
        checkLoad();
        return pms.getNext();
    }

    private void checkLoad() throws IOException {
        if (!pms.hasNext() && !completeLoad) {
            int size = pms.size();
            pms.addAll(readPage(size));
            if (size == pms.size())
                completeLoad = true;
        }
    }

    /**
     * Reloads the first page of this folder to search for new PMs.
     *
     * @return Any new PMs that were loaded.
     */
    public List<PrivateMessage> getNewPms() throws IOException {
        PrivateMessage             first = pms.get(0);
        boolean                    loadNext = true;
        int                        index = 0;
        LinkedList<PrivateMessage> output = new LinkedList<>();

        while (loadNext) {
            LinkedList<PrivateMessage> chk = readPage(index++);
            Iterator<PrivateMessage>   iter = chk.iterator();
            while (iter.hasNext()) {
                if (iter.next().getId() <= first.getId()) {
                    iter.remove();
                    loadNext = false;
                }
            }
            output.addAll(chk);
        }

        pms.addAll(output);

        return output;
    }

    protected abstract void deleteMessageFromFolder(int id) throws IOException;

    protected abstract void readMessage(PrivateMessage msg) throws IOException;

    protected abstract LinkedList<PrivateMessage> readPage(int start) throws IOException;

    public abstract ForumContext getContext();
}
