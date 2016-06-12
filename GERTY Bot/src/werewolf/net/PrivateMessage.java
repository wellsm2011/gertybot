package werewolf.net;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

public class PrivateMessage implements Comparable<PrivateMessage> {
    private LinkedList<ForumUser> to;
    private LinkedList<ForumUser> bcc;
    private ForumUser             from;
    private String                subject;
    private String                message;
    private int                   id;
    private boolean               read;
    private PmFolder              folder;
    private boolean               initalized = false;
    private DateTime              timestamp;

    public PrivateMessage(LinkedList<ForumUser> to, LinkedList<ForumUser> bcc, ForumUser from, String subject,
                          String message, int id, DateTime timestamp, PmFolder folder) {
        this(subject, true, id, timestamp, folder);
        initalize(from, to, bcc, message);
    }

    public PrivateMessage(String subject, boolean read, int id, DateTime timestamp, PmFolder folder) {
        to = null;
        bcc = null;
        from = null;
        this.subject = subject;
        message = null;
        this.folder = folder;
        this.id = id;
        this.timestamp = timestamp;
        this.read = read;
    }

    public void initalize(ForumUser from, LinkedList<ForumUser> to, LinkedList<ForumUser> bcc, String message) {
        if (initalized)
            return;
        read = true;
        initalized = true;
        this.from = from;
        this.to = to;
        this.bcc = bcc;
        this.message = message;
    }

    private void checkInit() throws IOException {
        if (!initalized)
            folder.readMessage(this);
    }

    public List<ForumUser> getTo() throws IOException {
        checkInit();
        return to;
    }

    public List<ForumUser> getBcc() throws IOException {
        checkInit();
        return bcc;
    }

    public ForumUser getFrom() throws IOException {
        checkInit();
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() throws IOException {
        checkInit();
        return message;
    }

    public int getId() {
        return id;
    }

    public void replyToSender(String subject, String message) throws IOException {
        getContext().makePm(new String[] { from.getName() }, new String[0], subject, message);
    }

    public void replyToSender(String message) throws IOException {
        String newSub = subject;
        if (!subject.startsWith("Re:"))
            newSub = "Re:" + newSub;
        replyToSender(newSub, message);
    }

    public void replyToAll(String subject, String message) throws IOException {
        checkInit();
        LinkedList<String> list = new LinkedList();
        for (ForumUser user : to) {
            if (!user.equals(getContext().LOGIN_USER))
                list.add(user.getName());
        }
        if (!from.equals(getContext().LOGIN_USER) || list.isEmpty())
            list.add(from.getName());
        getContext().makePm((String[])list.toArray(), new String[0], subject, message);
    }

    public void replyToAll(String message) throws IOException {
        checkInit();
        String newSub = subject;
        if (!subject.startsWith("Re:"))
            newSub = "Re:" + newSub;
        replyToAll(newSub, message);
    }

    public void deleteMessage() throws IOException {
        folder.deleteMessage(this);
    }

    public ForumContext getContext() {
        return folder.getContext();
    }

    @Override
    public int compareTo(PrivateMessage oth) {
        return oth.id - id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrivateMessage)
            return ((PrivateMessage)obj).id == id;
        return false;
    }
}
