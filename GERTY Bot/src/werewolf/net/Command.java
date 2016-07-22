package werewolf.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The command class represents a single interaction from the user to the bot.
 * Commands are parsed by the ForumThread class and the raw text is fed into
 * this class. Commands may be hidden to render them invisible to human users,
 * or invalidated to prevent the bot from using them and to show human users
 * that they are not being used.
 */
public class Command implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(Command.class.getName());

	private static final long serialVersionUID = -8803103421504120775L;

	private String		fullCommand;
	private String		command;
	private String		params;
	private ForumUser	user;
	private boolean		invalidated;
	// True if the bot is currently checking this invalid command for validity.
	private boolean		checking	= false;
	private ForumPost	post		= null;

	private int postCounter = 0;

	/**
	 * Creates a new command object with an attached post. This will allow
	 * invalidate() and hide() to work.
	 *
	 * @param command
	 *            The raw text of the command. The command class will parse this
	 *            into a name and param(s).
	 * @param user
	 *            The forum user that made the command.
	 * @param invalidated
	 *            True if the command is already invalid.
	 * @param hidden
	 *            True if the command is already hidden.
	 * @param post
	 *            The post associated with this command.
	 */
	public Command(String command, ForumUser user, boolean invalidated, ForumPost post)
	{
		this.fullCommand = command;
		command = command.trim().replaceAll("^\\[+|\\]+$", "").trim();
		this.command = command.replaceFirst(" +.*$", "");
		this.params = command.replaceFirst("^[^ ]+ +", "");
		this.user = user;
		this.invalidated = invalidated;
		this.post = post;
	}

	/**
	 * @param o
	 *            The object to compare this command to.
	 * @return Returns true if the given command is the same as this command.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Command)
		{
			Command other = (Command) o;
			if (!this.user.equals(other.user))
				return false;
			if (!this.params.equals(other.params))
				return false;
			return this.command.equals(other.command);
		}
		return false;
	}

	/**
	 * @return The name of the command used.
	 */
	public String getCommand()
	{
		return this.command;
	}

	/**
	 * @param expected
	 * @return Any additional text supplied with this command as an array of
	 *         strings split along any commas in the original string.
	 */
	public List<String> getParams(int expected)
	{
		return Arrays.asList(this.params.split(", ?", expected));
	}

	/**
	 * @return Any additional text supplied with the command as a single string.
	 */
	public String getParamString()
	{
		return this.params;
	}

	/**
	 * @return Returns the post associated with this command, or null if there
	 *         is no post.
	 */
	public ForumPost getPost()
	{
		return this.post;
	}

	/**
	 * @return Returns the post ID associated with this command, or -1 if there
	 *         is no post.
	 */
	public int getPostId()
	{
		if (this.post == null)
			return -1;
		return this.post.getPostId();
	}

	/**
	 * @return The forum user who made the command.
	 */
	public ForumUser getUser()
	{
		return this.user;
	}

	/**
	 * Marks the command as invalid and edits the post (if editing is allowed)
	 * to reflect the invalid state.
	 *
	 * @param reason
	 *            The reason for invalidation. Used in the edit as an
	 *            explination.
	 */
	public void invalidate(String reason)
	{
		this.checking = false;
		if (this.invalidated)
			return;
		String infoString = "\nCommand: " + this.command + "\nLocation: " + this.post.getUrl() + "\nReason: " + reason + "\nContext: " + this.post.getContext().toString();
		try
		{
			if (this.postCounter == 0)
				Command.LOGGER.info("Invalid command." + infoString);

			ForumPostEditor editor = this.post.getEditor();
			String replaceCommand = "[color=#FF0000][b]" + this.fullCommand + "[/b] (" + reason + ")[/color]";
			// TODO: This doesn't work properly if the user has multiples of the
			// same invalid command. Fix.
			editor.setText(editor.getText().replaceFirst("\\[b\\]" + Pattern.quote(this.fullCommand) + "\\[\\/b\\]", replaceCommand));
			editor.submit();
			this.invalidated = true;
		} catch (IOException e)
		{
			if (this.postCounter++ < 5)
			{
				Command.LOGGER.warning("IOException while trying to invalidate post. Context: " + this.post.getContext().toString());
				this.invalidate(reason);
			} else
				Command.LOGGER.warning("IOException while trying to invalidate post. Unable to invalidate. Context: " + this.post.getContext().toString());
		} catch (IllegalStateException e)
		{
			Command.LOGGER.warning("IllegalStateException while trying to invalidate post. Context: " + this.post.getContext().toString());
		}

		this.postCounter = 0;
	}

	/**
	 * @return True if invalidate() and validate() have not been called since
	 *         this command entered checking state.
	 */
	public boolean isChecking()
	{
		return this.checking;
	}

	/**
	 * @return True if the command has been marked as invalid.
	 */
	public boolean isInvalidated()
	{
		return this.invalidated;
	}

	/**
	 * Marks this command as being checked for validity. Does nothing if the
	 * command has not previously been marked as invalid.
	 */
	public void startCheck()
	{
		this.checking = this.invalidated;
	}

	/**
	 * @return A string representation of this command.
	 */
	@Override
	public String toString()
	{
		return this.getCommand() + ":" + this.getParamString() + " - " + this.getUser();
	}

	/**
	 * Marks a previously invalid command as valid and edits the post (if
	 * editing is allowed) to reflect the valid state.
	 */
	public void validate()
	{
		if (!this.invalidated)
			return;
		this.checking = false;
		// TODO: Implement validation logic.
	}
}
