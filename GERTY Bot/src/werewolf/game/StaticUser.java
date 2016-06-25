package werewolf.game;

import java.util.LinkedList;
import java.util.List;

public class StaticUser implements User
{
	// private final static Logger LOGGER =
	// Logger.getLogger(StaticUser.class.getName());

	/**
	 * The user representing a lack of a vote. All players default to voting for
	 * this user.
	 */
	public static final StaticUser			NOVOTE				= new StaticUser("No Vote");

	/**
	 * The user representing no lynch. Allows players to <b>[vote no lynch]</b>
	 * in games.
	 */
	public static final StaticUser			NOLYNCH				= new StaticUser("No Lynch");

	/**
	 * The user representing no king in kingmaker games. Allows players to
	 * <b>[vote no king]</b> in games.
	 */
	public static final StaticUser			NOKING				= new StaticUser("No King");

	/**
	 * The user representing a lack of the ability to vote. Any player marked as
	 * voting for this is either injured, not playing, or dead.
	 */
	public static final StaticUser			INCAPACITATED		= new StaticUser("Incapacitated");

	public static final List<StaticUser>	LIST;

	private static final long				serialVersionUID	= -9045376129902662803L;

	static
	{
		LIST = new LinkedList<>();
		StaticUser.LIST.add(StaticUser.NOLYNCH);
		StaticUser.LIST.add(StaticUser.NOKING);
		StaticUser.LIST.add(StaticUser.NOVOTE);
		StaticUser.LIST.add(StaticUser.INCAPACITATED);
	}

	private String							name;

	private StaticUser(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof StaticUser))
			return false;
		return ((StaticUser) o).getName().equals(this.getName());
	}

	/**
	 * @return The name of this static user.
	 */
	@Override
	public String getName()
	{
		return this.name;
	}
}
