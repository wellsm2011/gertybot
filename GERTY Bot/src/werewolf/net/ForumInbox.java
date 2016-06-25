package werewolf.net;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public abstract class ForumInbox
{
	private static class LinkedPmList implements Iterable<PrivateMessage>
	{
		TreeMap<Integer, PrivateMessage>	tree			= new TreeMap<>();
		PrivateMessage						currentElement	= null;

		public boolean add(PrivateMessage e)
		{
			return this.tree.putIfAbsent(-e.getId(), e) != null;
		}

		public boolean addAll(Collection<? extends PrivateMessage> c)
		{
			boolean ret = false;
			for (PrivateMessage msg : c)
				ret = ret || this.add(msg);
			return ret;
		}

		public void clear()
		{
			this.tree.clear();
			this.currentElement = null;
		}

		public boolean contains(PrivateMessage o)
		{
			return this.tree.containsValue(o);
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof LinkedPmList)
				return this.tree.equals(((LinkedPmList) o).tree);
			return false;
		}

		public PrivateMessage get(int index)
		{
			for (PrivateMessage msg : this.tree.values())
				if (index-- == 0)
					return msg;
			return null;
		}

		public int getCurrentIndex()
		{
			return this.indexOf(this.currentElement);
		}

		public PrivateMessage getNext()
		{
			if (this.currentElement == null)
				this.currentElement = this.get(0);
			else
				this.currentElement = this.getNext(this.currentElement);
			return this.currentElement;
		}

		public PrivateMessage getNext(PrivateMessage previous)
		{
			boolean returnNext = false;
			for (PrivateMessage msg : this.tree.values())
			{
				if (returnNext == true)
					return msg;
				if (msg.equals(previous))
					returnNext = true;
			}
			return null;
		}

		public PrivateMessage getPrevious()
		{
			if (this.currentElement == null)
				this.currentElement = this.get(this.size() - 1);
			else
				this.currentElement = this.getPrevious(this.currentElement);
			return this.currentElement;
		}

		public PrivateMessage getPrevious(PrivateMessage next)
		{
			PrivateMessage ret = null;
			for (PrivateMessage msg : this.tree.values())
			{
				if (msg.equals(next))
					return ret;
				ret = msg;
			}
			return null;
		}

		@Override
		public int hashCode()
		{
			return this.tree.hashCode();
		}

		public boolean hasNext()
		{
			return this.getCurrentIndex() != this.size() - 1;
		}

		public boolean hasPrevious()
		{
			for (PrivateMessage msg : this.tree.values())
			{
				if (!msg.equals(this.currentElement))
					return this.currentElement != null;
				break;
			}
			return false;
		}

		public int indexOf(PrivateMessage o)
		{
			int index = 0;
			for (PrivateMessage msg : this.tree.values())
			{
				if (msg.equals(o))
					return index;
				index++;
			}
			return -1;
		}

		public boolean isEmpty()
		{
			return this.tree.isEmpty();
		}

		@Override
		public Iterator<PrivateMessage> iterator()
		{
			return this.tree.values().iterator();
		}

		public boolean remove(PrivateMessage msg)
		{
			if (msg.equals(this.currentElement))
				this.currentElement = this.getPrevious(this.currentElement);
			return this.tree.remove(-msg.getId()) != null;
		}

		public int size()
		{
			return this.tree.size();
		}
	}

	private LinkedPmList	pms				= new LinkedPmList();
	private boolean			completeLoad	= false;
	protected final String	folderName;

	public ForumInbox(String folderName)
	{
		this.folderName = folderName;
	}

	private void checkLoad() throws IOException
	{
		if (!this.pms.hasNext() && !this.completeLoad)
		{
			int size = this.pms.size();
			this.pms.addAll(this.readPage(size));
			if (size == this.pms.size())
				this.completeLoad = true;
		}
	}

	protected void deleteMessage(PrivateMessage msg) throws IOException, IllegalArgumentException
	{
		if (!this.pms.contains(msg))
			throw new IllegalArgumentException("Message not found in folder.");
		this.deleteMessageFromFolder(msg.getId());
		this.pms.remove(msg);
	}

	protected abstract void deleteMessageFromFolder(int id) throws IOException;

	public abstract ForumContext getContext();

	public PrivateMessage getNextPm() throws IOException
	{
		this.checkLoad();
		return this.pms.getNext();
	}

	protected abstract void readMessage(PrivateMessage msg) throws IOException;

	protected abstract LinkedList<PrivateMessage> readPage(int start) throws IOException;

	/**
	 * Reloads the first page of this folder to search for new PMs.
	 *
	 * @return Any new PMs that were loaded.
	 */
	public List<PrivateMessage> update() throws IOException
	{
		PrivateMessage first = this.pms.get(0);
		boolean loadNext = true;
		int index = 0;
		LinkedList<PrivateMessage> output = new LinkedList<>();

		while (loadNext)
		{
			LinkedList<PrivateMessage> chk = this.readPage(index++);
			Iterator<PrivateMessage> iter = chk.iterator();
			while (iter.hasNext())
				if (iter.next().getId() <= first.getId())
				{
					iter.remove();
					loadNext = false;
				}
			output.addAll(chk);
		}

		this.pms.addAll(output);

		return output;
	}
}