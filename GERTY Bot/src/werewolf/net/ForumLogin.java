package werewolf.net;

public class ForumLogin extends ForumUser
{
	private static final long serialVersionUID = -8865347377571642240L;

	public final String password;

	/**
	 * @param oth
	 *            The forum user this login is for.
	 * @param password
	 *            The password for the user's account.
	 */
	public ForumLogin(ForumUser oth, String password)
	{
		super(oth);
		this.password = password;
	}

	/**
	 * @param u
	 *            The user ID of this user.
	 * @param name
	 *            The displayed name of this user.
	 * @param password
	 *            The password to access this user's account.
	 * @param context
	 *            The forum context this user belongs to.
	 */
	public ForumLogin(int u, String name, String password, ForumContext context)
	{
		super(u, name, context);
		this.password = password;
	}

	/**
	 * @return The password for this user's account.
	 */
	public String getPassword()
	{
		return this.password;
	}
}
