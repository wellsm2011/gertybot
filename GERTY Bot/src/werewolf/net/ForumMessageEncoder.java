package werewolf.net;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import werewolf.net.msg.ForumMessageBold;
import werewolf.net.msg.ForumMessageCodeblock;
import werewolf.net.msg.ForumMessageColor;
import werewolf.net.msg.ForumMessageContainer;
import werewolf.net.msg.ForumMessageElement;
import werewolf.net.msg.ForumMessageItalic;
import werewolf.net.msg.ForumMessageQuote;
import werewolf.net.msg.ForumMessageSpoiler;
import werewolf.net.msg.ForumMessageStrike;
import werewolf.net.msg.ForumMessageString;
import werewolf.net.msg.ForumMessageUrl;

/**
 * This class handles messages passed from the game package. Classes that extend
 * this class implement functionality that translates rich text to a plaintext
 * output. For example, a BBCode implementation of this class might translate
 * bold text to become [b]text[/b] while an html implementation might translate
 * it to &lt;b&gt;text&lt;/b&gt;.
 * 
 * @author Michael Wells
 */
public abstract class ForumMessageEncoder
{
	private static final Logger				LOGGER		= Logger.getLogger(ForumMessageEncoder.class.getName());
	/**
	 * The default implementation, which eliminates richtext. Suitable for
	 * console or logfile output.
	 */
	public static final ForumMessageEncoder	PLAINTEXT	= new ForumMessageEncoder()
	{
		@Override
		protected String encodeBold(String msg)
		{
			return msg;
		}

		@Override
		protected String encodeCodeblock(String msg)
		{
			return msg;
		}

		@Override
		protected String encodeColor(String msg, Color color)
		{
			return msg;
		}

		@Override
		protected String encodeHeader(String msg)
		{
			return msg.toUpperCase();
		}

		@Override
		protected String encodeItalic(String msg)
		{
			return msg;
		}

		@Override
		protected String encodeQuote(String msg, String author)
		{
			if (msg.contains("\n"))
				return "Quote (" + author + "):\n\t" + msg.replaceAll("\n", "\n\t");
			return "\"" + msg + "\" (" + author + ")";
		}

		@Override
		protected String encodeSpoiler(String msg, String title)
		{
			return this.encodeHeader(title) + ":\n" + msg;
		}

		@Override
		protected String encodeStrike(String msg)
		{
			return msg;
		}

		@Override
		protected String encodeUrl(String msg, String url)
		{
			return msg + "<" + url + ">";
		}

		@Override
		protected String escape(String msg)
		{
			return msg;
		}

	};

	protected abstract String encodeBold(String msg);

	protected abstract String encodeCodeblock(String msg);

	protected abstract String encodeColor(String msg, Color color);

	protected abstract String encodeHeader(String msg);

	protected abstract String encodeItalic(String msg);

	/**
	 * Function to perform the encoding of a forum message element.
	 * 
	 * @param msg
	 *            The message to encode.
	 * @return The encoded form of the given message, as a String.
	 */
	public String encodeMessage(ForumMessageElement msg)
	{
		if (this != ForumMessageEncoder.PLAINTEXT && ForumMessageEncoder.LOGGER.isLoggable(Level.FINE))
			ForumMessageEncoder.LOGGER.fine("Encoding Message: " + ForumMessageEncoder.PLAINTEXT.encodeMessage(msg));

		// Figure out which element we're dealing with and call the
		// associated implementation-specific function.
		return msg.toString((elem, string) -> {
			if (elem instanceof ForumMessageContainer)
				return string;	// Container is just a grouping.
			if (elem instanceof ForumMessageString)
				return this.escape(((ForumMessageString) elem).getMsg());
			if (elem instanceof ForumMessageBold)
				return this.encodeBold(string);
			if (elem instanceof ForumMessageItalic)
				return this.encodeItalic(string);
			if (elem instanceof ForumMessageStrike)
				return this.encodeStrike(string);
			if (elem instanceof ForumMessageSpoiler)
				return this.encodeSpoiler(string, ((ForumMessageSpoiler) elem).getTitle());
			if (elem instanceof ForumMessageCodeblock)
				return this.encodeCodeblock(string);
			if (elem instanceof ForumMessageQuote)
				return this.encodeQuote(string, ((ForumMessageQuote) elem).getAuthor());
			if (elem instanceof ForumMessageColor)
				return this.encodeColor(string, ((ForumMessageColor) elem).getColor());
			if (elem instanceof ForumMessageUrl)
				return this.encodeUrl(string, ((ForumMessageUrl) elem).getUrl());
			throw new IllegalArgumentException("Unknown element type in message: " + elem.getClass().toString());
		});
	}

	protected abstract String encodeQuote(String msg, String author);

	protected abstract String encodeSpoiler(String msg, String title);

	protected abstract String encodeStrike(String msg);

	protected abstract String encodeUrl(String msg, String url);

	protected abstract String escape(String msg);
}
