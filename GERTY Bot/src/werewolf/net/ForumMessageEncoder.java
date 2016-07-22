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
		return msg.toString((elm, inner) -> {
			if (elm instanceof ForumMessageContainer)
				return inner;	// Container is just a grouping.
			if (elm instanceof ForumMessageString)
				return ForumMessageEncoder.this.escape(((ForumMessageString) elm).getMsg());
			if (elm instanceof ForumMessageBold)
				return ForumMessageEncoder.this.encodeBold(inner);
			if (elm instanceof ForumMessageItalic)
				return ForumMessageEncoder.this.encodeItalic(inner);
			if (elm instanceof ForumMessageStrike)
				return ForumMessageEncoder.this.encodeStrike(inner);
			if (elm instanceof ForumMessageSpoiler)
				return ForumMessageEncoder.this.encodeSpoiler(inner, ((ForumMessageSpoiler) elm).getTitle());
			if (elm instanceof ForumMessageCodeblock)
				return ForumMessageEncoder.this.encodeCodeblock(inner);
			if (elm instanceof ForumMessageQuote)
				return ForumMessageEncoder.this.encodeQuote(inner, ((ForumMessageQuote) elm).getAuthor());
			if (elm instanceof ForumMessageColor)
				return ForumMessageEncoder.this.encodeColor(inner, ((ForumMessageColor) elm).getColor());
			if (elm instanceof ForumMessageUrl)
				return ForumMessageEncoder.this.encodeUrl(inner, ((ForumMessageUrl) elm).getUrl());
			throw new IllegalArgumentException("Unknown element type in message: " + elm.getClass().toString());
		});
	}

	protected abstract String encodeQuote(String msg, String author);

	protected abstract String encodeSpoiler(String msg, String title);

	protected abstract String encodeStrike(String msg);

	protected abstract String encodeUrl(String msg, String url);

	protected abstract String escape(String msg);
}
