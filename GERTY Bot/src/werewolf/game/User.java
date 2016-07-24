package werewolf.game;

import java.io.Serializable;

public interface User extends Serializable
{
	public default int compareTo(User a)
	{
		return this.getName().compareTo(a.getName());
	}

	public String getName();;
}
