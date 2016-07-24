package werewolf.game;

import java.io.Serializable;

public interface User extends Serializable
{
	public String getName();

	public default int compareTo(User a)
	{
		return this.getName().compareTo(a.getName());
	};
}
