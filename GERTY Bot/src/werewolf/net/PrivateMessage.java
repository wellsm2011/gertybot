package werewolf.net;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

public class PrivateMessage implements Comparable<PrivateMessage>
{
	private List<ForumUser>	to			= null;
	private List<ForumUser>	bcc			= null;
	private ForumUser		from;
	private String			subject;
	private String			message;
	private int				id;
	private boolean			read;
	private ForumInbox		inbox;
	private boolean			initalized	= false;
	private DateTime		timestamp;

	public PrivateMessage(List<ForumUser> to, List<ForumUser> bcc, ForumUser from, String subject, String message, int id, DateTime timestamp, ForumInbox folder)
	{
		this(subject, true, id, timestamp, folder);
		this.initalize(from, to, bcc, message);
	}

	public PrivateMessage(String subject, boolean read, int id, DateTime timestamp, ForumInbox inbox)
	{
		this.to = null;
		this.bcc = null;
		this.from = null;
		this.subject = subject;
		this.message = null;
		this.inbox = inbox;
		this.id = id;
		this.timestamp = timestamp;
		this.read = read;
	}

	private void checkInit() throws IOException
	{
		if (!this.initalized)
			this.inbox.readMessage(this);
	}

	@Override
	public int compareTo(PrivateMessage oth)
	{
		return oth.id - this.id;
	}

	public void delete() throws IOException
	{
		this.inbox.deleteMessage(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PrivateMessage)
			return ((PrivateMessage) obj).id == this.id;
		return false;
	}

	public List<ForumUser> getBcc() throws IOException
	{
		this.checkInit();
		return this.bcc;
	}

	public ForumContext getContext()
	{
		return this.inbox.getContext();
	}

	public ForumUser getFrom() throws IOException
	{
		this.checkInit();
		return this.from;
	}

	public int getId()
	{
		return this.id;
	}

	public String getMessage() throws IOException
	{
		this.checkInit();
		return this.message;
	}

	public String getSubject()
	{
		return this.subject;
	}

	public DateTime getTimestamp()
	{
		return this.timestamp;
	}

	public List<ForumUser> getTo() throws IOException
	{
		this.checkInit();
		return this.to;
	}
	
	public boolean hasBeenRead() {
		return read;
	}

	@Override
	public int hashCode()
	{
		return this.id;
	}

	public void initalize(ForumUser from, List<ForumUser> to, List<ForumUser> bcc, String message)
	{
		if (this.initalized)
			return;
		this.read = true;
		this.initalized = true;
		this.from = from;
		this.to = to;
		this.bcc = bcc;
		this.message = message;
	}

	public void replyToAll(String message) throws IOException
	{
		this.checkInit();
		String newSub = this.subject;
		if (!this.subject.startsWith("Re:"))
			newSub = "Re:" + newSub;
		this.replyToAll(newSub, message);
	}

	public void replyToAll(String subject, String message) throws IOException
	{
		this.checkInit();
		LinkedList<String> list = new LinkedList<>();
		for (ForumUser user : this.to)
			if (!user.equals(this.getContext().LOGIN_USER))
				list.add(user.getName());
		if (!this.from.equals(this.getContext().LOGIN_USER) || list.isEmpty())
			list.add(this.from.getName());
		this.getContext().makePm((String[]) list.toArray(), new String[0], subject, message);
	}

	public void replyToSender(String message) throws IOException
	{
		String newSub = this.subject;
		if (!this.subject.startsWith("Re:"))
			newSub = "Re:" + newSub;
		this.replyToSender(newSub, message);
	}

	public void replyToSender(String subject, String message) throws IOException
	{
		this.getContext().makePm(new String[]
		{ this.from.getName() }, new String[0], subject, message);
	}
}
