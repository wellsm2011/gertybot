package werewolf.game;

import java.io.Serializable;

public class PlayerEvent implements Serializable
{
	// private final static Logger LOGGER =
	// Logger.getLogger(PlayerEvent.class.getName());

	private static final long	serialVersionUID	= 8596614734212157041L;

	String						text;
	int							round;
	String						postLink;

	/**
	 * Creates a new player event.
	 *
	 * @param text
	 *            The text to display on the event.
	 * @param round
	 *            The round this event occured.
	 * @param postLink
	 *            URL to the post this event was created.
	 */
	public PlayerEvent(String text, int round, String postLink)
	{
		this.text = text.substring(0, 1).toUpperCase() + text.substring(1);
		this.round = round;
		this.postLink = postLink;
	}

	/**
	 * @return A plaintext verson of this event.
	 */
	public String getData()
	{
		return this.text + " R" + this.round;
	}

	/**
	 * @return The round when this event was logged.
	 */
	public String getRound()
	{
		return "R" + this.round;
	}

	/**
	 * @param text
	 *            The text to display inside the link.
	 * @return A BBCode-formatted link to this event containing the given text.
	 */
	public String makeLink(String text)
	{
		return "[url=" + this.postLink + "]" + text + "[/url]";
	}
}
