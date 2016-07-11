package werewolf.net;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import werewolf.net.msg.*;

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
															protected String escape(String msg)
															{
																return msg;
															}

															@Override
															protected String encodeBold(String msg)
															{
																return msg;
															}

															@Override
															protected String encodeItalic(String msg)
															{
																return msg;
															}

															@Override
															protected String encodeStrike(String msg)
															{
																return msg;
															}

															@Override
															protected String encodeHeader(String msg)
															{
																return msg.toUpperCase();
															}

															@Override
															protected String encodeSpoiler(String msg, String title)
															{
																return encodeHeader(title) + ":\n" + msg;
															}

															@Override
															protected String encodeCodeblock(String msg)
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
															protected String encodeColor(String msg, Color color)
															{
																return msg;
															}

															@Override
															protected String encodeUrl(String msg, String url)
															{
																return msg + "<" + url + ">";
															}

														};

	/**
	 * Function to perform the encoding of a forum message element.
	 * 
	 * @param msg
	 *            The message to encode.
	 * @return The encoded form of the given message, as a String.
	 */
	public String encodeMessage(ForumMessageElement msg)
	{
		if (this != PLAINTEXT && LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Encoding Message: " + PLAINTEXT.encodeMessage(msg));

		// Figure out which element we're dealing with and call the
		// associated implementation-specific function.
		return msg.toString((ForumMessageElement elm, String inner) -> {
			if (elm instanceof ForumMessageContainer)
				return inner;	// Container is just a grouping.
			if (elm instanceof ForumMessageString)
				return escape(((ForumMessageString) elm).getMsg());
			if (elm instanceof ForumMessageBold)
				return encodeBold(inner);
			if (elm instanceof ForumMessageItalic)
				return encodeItalic(inner);
			if (elm instanceof ForumMessageStrike)
				return encodeStrike(inner);
			if (elm instanceof ForumMessageSpoiler)
				return encodeSpoiler(inner, ((ForumMessageSpoiler) elm).getTitle());
			if (elm instanceof ForumMessageCodeblock)
				return encodeCodeblock(inner);
			if (elm instanceof ForumMessageQuote)
				return encodeQuote(inner, ((ForumMessageQuote) elm).getAuthor());
			if (elm instanceof ForumMessageColor)
				return encodeColor(inner, ((ForumMessageColor) elm).getColor());
			if (elm instanceof ForumMessageUrl)
				return encodeUrl(inner, ((ForumMessageUrl) elm).getUrl());
			throw new IllegalArgumentException("Unknown element type in message: " + elm.getClass().toString());
		});
	}

	protected abstract String escape(String msg);

	protected abstract String encodeBold(String msg);

	protected abstract String encodeItalic(String msg);

	protected abstract String encodeStrike(String msg);

	protected abstract String encodeHeader(String msg);

	protected abstract String encodeSpoiler(String msg, String title);

	protected abstract String encodeCodeblock(String msg);

	protected abstract String encodeQuote(String msg, String author);

	protected abstract String encodeColor(String msg, Color color);

	protected abstract String encodeUrl(String msg, String url);
}
