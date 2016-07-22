package werewolf.experimental;

import java.awt.Color;
import java.util.logging.Logger;

/**
 * This class handles messages passed from the game package. Classes that extend
 * this class implement functionality that translates rich text to a plaintext
 * output. For example, a BBCode implementation of this class might translate
 * bold text to become [b]text[/b] while an html implementation might translate
 * it to &lt;b&gt;text&lt;/b&gt;.
 * 
 * @author Michael Wells
 * @author Andrew Binns
 */
public abstract class ForumMessageEncoder
{
	private static final Logger LOGGER = Logger.getLogger(ForumMessageEncoder.class.getName());

	/**
	 * The default implementation, which eliminates richtext. Suitable for
	 * console or logfile output.
	 */

	public String encodeMessageElement(ForumMessageElement elem)
	{
		return null;
	}

	protected abstract String encodeQuote(String msg, String author);

	protected abstract String encodeSpoiler(String msg, String title);

	protected abstract String encodeColor(String msg, Color color);

	protected abstract String encodeUrl(String msg, String url);

	protected abstract String encodeStrike(String msg);

	protected abstract String encodeEscaped(String msg);

	protected abstract String encodeBold(String msg);

	protected abstract String encodeCodeblock(String msg);

	protected abstract String encodeHeader(String msg);

	protected abstract String encodeItalic(String msg);

	public static final ForumMessageEncoder PLAINTEXT = new ForumMessageEncoder()
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

}
