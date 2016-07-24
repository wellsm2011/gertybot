package werewolf.net;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Message
{
	private class RawText extends Op
	{
		public RawText(String msg)
		{
			super(null, msg);
		}
	}

	private abstract class Op
	{
		private Style		style;
		private String		msg;
		private Object[]	params;

		protected Op(Style s, String msg, Object... params)
		{
			this.style = s;
			this.msg = msg;
			this.params = params;
		}

		public String getMsg()
		{
			return this.msg;
		}

		public Object[] getParams()
		{
			return this.params;
		}

		public Style getStyle()
		{
			return this.style;
		}
	}

	private class StartStyle extends Op
	{
		public StartStyle(Style s, Object... params)
		{
			super(s, null, params);
		}
	}

	private class StopStyle extends Op
	{
		public StopStyle(Style s, Object... params)
		{
			super(s, null, params);
		}
	}

	public enum Style
	{
		QUOTE,
		SPOILER,
		STRIKE,
		URL,
		BOLD,
		ITALIC,
		COLOR,
		HEADER,
		UNDERLINE,
		CODE,
		LIST,
		LISTITEM
	}

	// Default color for names to be posted in.
	public static final Color	VILLAGE		= Color.GREEN;
	public static final Color	EVIL		= Color.RED;
	public static final Color	NEUTRAL		= Color.YELLOW;
	public static final Color	THIRD_PARTY	= new Color(0xFF2DC3);
	public static final Color	DEAD		= new Color(0xFFBF80);
	public static final Color	INVALID		= Color.RED;

	/**
	 * Returns a simple forum message consisting of just the specified text.
	 * 
	 * @param text
	 * @return a forum message of that text
	 */
	public static Message of(String text)
	{
		return new Message().add(text);
	}

	private Set<Style> currentlyActive;

	private List<Op> operations;

	public Message()
	{
		this.currentlyActive = new LinkedHashSet<>();
		this.operations = new ArrayList<>();
	}

	/**
	 * <p>
	 * Given another forum message, adds that one's contents to this one. This
	 * is done by treating every operation supplied by the other message as a
	 * new operation triggered on this message. So, in effect; adding another
	 * message has the same effect as adding that message's contents
	 * individually. This means that if the current message at the start has
	 * some style going, additional start-style operations for the previously
	 * active style will be ignored. Additionally, the other message's stop
	 * style commands will stop the currently going styles.
	 * </p>
	 * <p>
	 * This operation does not effect the other message specified.
	 * </p>
	 * 
	 * @param otherMessage
	 *            the other message whose contents are to be added to this one
	 * @return this message for chaining
	 */
	public Message add(Message otherMessage)
	{
		for (Op o : otherMessage.operations)
		{
			if (o instanceof RawText)
				this.operations.add(o);
			if (o instanceof StopStyle)
				this.stopStyle(o.getStyle());
			if (o instanceof StartStyle)
				this.startStyle(o.getStyle(), o.getParams());
		}
		return this;
	}

	public Message add(String txt)
	{
		if (this.currentlyActive.contains(Style.LIST))
			this.startStyle(Style.LISTITEM);
		this.operations.add(new RawText(txt));
		if (this.currentlyActive.contains(Style.LIST))
			this.stopStyle(Style.LISTITEM);
		return this;
	}

	@Override
	public Message clone()
	{
		Message res = new Message();
		this.operations.forEach(res.operations::add);
		this.currentlyActive.forEach(res.currentlyActive::add);
		return res;
	}

	/**
	 * Given the specified encoder, encodes this message as a string. This
	 * allows
	 * 
	 * @param encoder
	 * @return
	 */
	public String formatString(ForumMessageEncoder encoder)
	{
		StringBuilder sb = new StringBuilder();

		for (Op op : this.operations)
		{
			if (op instanceof StartStyle)
				sb.append(encoder.getOpening(op.getStyle(), op.getParams()));
			if (op instanceof StopStyle)
				sb.append(encoder.getClosing(op.getStyle(), op.getParams()));
			if (op instanceof RawText)
				sb.append(encoder.escape(op.getMsg()));
		}
		return sb.toString();
	}

	/**
	 * @return the number of raw text segments in this message
	 */
	public int getTxtCount()
	{
		return (int) this.operations.stream().filter(o -> o instanceof RawText).count();
	}

	public boolean hasTextSegments()
	{
		return this.operations.stream().filter(o -> o instanceof RawText).findFirst().isPresent();
	}

	public Message startBold()
	{
		this.startStyle(Style.BOLD);
		return this;
	}

	public Message startCodeBlock()
	{
		this.startStyle(Style.CODE);
		return this;
	}

	public Message startColor(Color color)
	{
		this.startStyle(Style.BOLD, Integer.toString(color.getRGB() % 0xFFFFFF, 16));
		return this;
	}

	public Message startHeader()
	{
		this.startStyle(Style.HEADER);
		return this;
	}

	public Message startItalic()
	{
		this.startStyle(Style.ITALIC);
		return this;
	}

	public Message startQuote(String author)
	{
		this.startStyle(Style.QUOTE, author);
		return this;
	}

	public Message startSpoiler(String title)
	{
		this.startStyle(Style.SPOILER, title);
		return this;
	}

	public Message startStrike()
	{
		this.startStyle(Style.STRIKE);
		return this;
	}

	private void startStyle(Style s, Object... params)
	{
		if (this.currentlyActive.contains(s))
			return;
		this.currentlyActive.add(s);
		this.operations.add(new StartStyle(s, params));
	}

	public Message startURL(String link)
	{
		this.startStyle(Style.URL, link);
		return this;
	}

	/**
	 * ends any currently in-progress styles.
	 * 
	 * @return this message for chaining
	 */
	public Message stopAll()
	{
		// by stopping the oldest style, we stop them all.
		if (!this.currentlyActive.isEmpty())
			this.stopStyle(this.currentlyActive.iterator().next());
		return this;
	}

	public Message stopBold()
	{
		this.stopStyle(Style.BOLD);
		return this;
	}

	public Message stopCodeBlock()
	{
		this.stopStyle(Style.CODE);
		return this;
	}

	public Message stopColor()
	{
		this.stopStyle(Style.COLOR);
		return this;
	}

	public Message stopHeader()
	{
		this.stopStyle(Style.HEADER);
		return this;
	}

	public Message stopItalic()
	{
		this.stopStyle(Style.ITALIC);
		return this;
	}

	public Message stopQuote()
	{
		this.stopStyle(Style.QUOTE);
		return this;
	}

	public Message stopSpoiler()
	{
		this.stopStyle(Style.SPOILER);
		return this;
	}

	public Message stopStrike()
	{
		this.stopStyle(Style.STRIKE);
		return this;
	}

	private void stopStyle(Style s)
	{
		if (!this.currentlyActive.contains(s))
			return;
		Iterator<Style> active = this.currentlyActive.iterator();
		if (active.hasNext())
		{
			// Go up through our stack til we find style to stop
			Style cur = active.next();
			while (!cur.equals(s) && active.hasNext())
				cur = active.next();
			/*
			 * Make a deque of all the styles to remove, in reverse order; that
			 * is, the resulting deque should be in order of most recently added
			 * to stack first.
			 */
			Deque<Style> toStop = new ArrayDeque<>(Style.values().length);
			toStop.offerFirst(cur);
			while (active.hasNext())
				toStop.offerFirst(active.next());
			// Add stop operations for each style
			toStop.stream().map(style -> {
				/*
				 * To preserve the parameters from the start style, we must
				 * iterate back along the op history til we find the starting
				 * operator which starts the same style as the one we're trying
				 * to close. Once we do, break out an return the stop style with
				 * the appropriate params. Otherwise do one with just blank
				 * params.
				 */
				Object[] p = new Object[]
				{};
				ListIterator<Op> li = this.operations.listIterator(this.operations.size());
				while (li.hasPrevious())
				{
					Op prev = li.previous();
					if (prev instanceof StartStyle)
						if (prev.getStyle() == style)
						{
							p = prev.getParams();
							break;
						}
				}
				return new StopStyle(style, p);
			}).forEach(this.operations::add);
			// Remove the styles from the currently active set
			toStop.forEach(this.currentlyActive::remove);
		}
	}

	/**
	 * Stops the current URL style
	 * 
	 * @return this message for chaining
	 */
	public Message stopURL()
	{
		this.stopStyle(Style.URL);
		return this;
	}
}
