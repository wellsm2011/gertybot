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
public abstract class ForumMessageEncoder<T>
{
	private static final Logger				LOGGER		= Logger.getLogger(ForumMessageEncoder.class.getName());
	/**
	 * The default implementation, which eliminates richtext. Suitable for
	 * console or logfile output.
	 */


	public T encodeMessageElement(ForumMessageElement elem)
	{
		elem
		return null;
	}

	protected abstract T encodeQuote(T msg, T author);

	protected abstract T encodeSpoiler(T msg, T title);

	protected abstract T encodeStrike(T msg);

	protected abstract T encodeUrl(T msg, T url);

	protected abstract T escape(T msg);

	protected abstract T encodeBold(T msg);

	protected abstract T encodeCodeblock(T msg);

	protected abstract T encodeColor(T msg, Color color);

	protected abstract T encodeHeader(T msg);

	protected abstract T encodeItalic(T msg);
	
	public static final ForumMessageEncoder<String>	PLAINTEXT	= new ForumMessageEncoder<String>()
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
