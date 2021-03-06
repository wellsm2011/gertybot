package werewolf.net;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ForumUserDatabase implements Serializable
{
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private Map<Integer, ForumUser>	users				= new HashMap<>();
	private List<ForumUser>			unknownUsers		= new LinkedList<>();
	public final ForumContext		context;
	private boolean					initalized			= false;

	public ForumUserDatabase(ForumContext context)
	{
		this.context = context;
	}

	public void addUser(ForumUser user)
	{
		if (this.getUser(user.getName()) != null)
			return;

		if (user.getUserId() > 0)
			this.users.put(user.getUserId(), user);
		this.unknownUsers.add(user);
	}

	public Collection<ForumUser> getKnownUsers()
	{
		return this.users.values();
	}

	public ForumUser getUser(Integer u)
	{
		return this.users.get(u);
	}

	public ForumUser getUser(Integer u, String name)
	{
		ForumUser user = this.getUser(u);
		if (user != null)
			return user;
		return this.getUser(name);
	}

	public ForumUser getUser(String name)
	{
		boolean multipleAliases = false;
		ForumUser found = null;
		for (ForumUser user : this.users.values())
		{
			if (user.getName().equalsIgnoreCase(name))
				return user;
			for (String alias : user.getAliases())
				if (alias.equalsIgnoreCase(name))
					if (found == null)
						found = user;
					else
						multipleAliases = true;
		}
		if (!multipleAliases)
			return found;
		return null;
	}

	public ForumUser getUserFromExternalSource(String name)
	{
		return this.getUserFromExternalSource(name, this.users.values());
	}

	public <T extends ForumUser> T getUserFromExternalSource(String name, Collection<T> list)
	{
		for (T user : list)
			if (user.getName().equalsIgnoreCase(name))
				return user;

		T found = null;
		for (T user : list)
			if (user.getName().toLowerCase().startsWith(name.toLowerCase()))
			{
				if (found != null)
					return null;
				found = user;
			}
		if (found != null)
			return found;

		for (T user : list)
			if (user.getName().toLowerCase().contains(name.toLowerCase()))
			{
				if (found != null)
					return null;
				found = user;
			}
		if (found != null)
			return found;

		for (T user : list)
			for (String alias : user.getAliases())
				if (alias.toLowerCase().contains(name.toLowerCase()))
				{
					if (found != null && found != user)
						return null;
					found = user;
				}
		if (found != null)
			return found;

		try
		{
			int id = Integer.parseInt(name);
			for (T user : list)
				if (user.getUserId() == id)
					return user;
		} catch (NumberFormatException ex)
		{
		}

		return null;
	}

	public void userIdSet(ForumUser user)
	{
		// If valid id is present and user was previously known, log.
		if (user.getUserId() > 0 && this.unknownUsers.remove(user))
			this.users.put(user.getUserId(), user);
	}
}
