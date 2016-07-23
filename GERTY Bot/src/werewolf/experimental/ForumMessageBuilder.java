package werewolf.experimental;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class ForumMessageBuilder
{
	private class Message extends Op
	{
		public Message(String msg)
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

	enum Style
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

	private Set<Style>	currentlyActive;
	private List<Op>	operations;

	public ForumMessageBuilder()
	{
		this.currentlyActive = new LinkedHashSet<>();
		this.operations = new ArrayList<>();
	}

	public ForumMessageBuilder add(String txt)
	{
		if (this.currentlyActive.contains(Style.LIST))
			this.startStyle(Style.LISTITEM);
		this.operations.add(new Message(txt));
		if (this.currentlyActive.contains(Style.LIST))
			this.stopStyle(Style.LISTITEM);
		return this;
	}

	public String formatString(ForumMessageEncoder encoder)
	{
		StringBuilder sb = new StringBuilder();

		for (Op op : this.operations)
		{
			if (op instanceof StartStyle)
				sb.append(encoder.getOpening(op.getStyle(), op.getParams()));
			if (op instanceof StopStyle)
				sb.append(encoder.getClosing(op.getStyle(), op.getParams()));
			if (op instanceof Message)
				sb.append(encoder.escape(op.getMsg()));
		}
		return sb.toString();
	}

	public ForumMessageBuilder startBold()
	{
		this.startStyle(Style.BOLD);
		return this;
	}

	public ForumMessageBuilder startCodeBlock()
	{
		this.startStyle(Style.CODE);
		return this;
	}

	public ForumMessageBuilder startColor(Color color)
	{
		this.startStyle(Style.BOLD, Integer.toString(color.getRGB() % 0xFFFFFF, 16));
		return this;
	}

	public ForumMessageBuilder startHeader()
	{
		this.startStyle(Style.HEADER);
		return this;
	}

	public ForumMessageBuilder startItalic()
	{
		this.startStyle(Style.ITALIC);
		return this;
	}

	public ForumMessageBuilder startQuote(String author)
	{
		this.startStyle(Style.QUOTE, author);
		return this;
	}

	public ForumMessageBuilder startSpoiler(String title)
	{
		this.startStyle(Style.SPOILER, title);
		return this;
	}

	public ForumMessageBuilder startStrike()
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

	public ForumMessageBuilder startURL(String link)
	{
		this.startStyle(Style.BOLD, link);
		return this;
	}

	public ForumMessageBuilder stopBold()
	{
		this.stopStyle(Style.BOLD);
		return this;
	}

	public ForumMessageBuilder stopCodeBlock()
	{
		this.stopStyle(Style.CODE);
		return this;
	}

	public ForumMessageBuilder stopColor()
	{
		this.stopStyle(Style.BOLD);
		return this;
	}

	public ForumMessageBuilder stopHeader()
	{
		this.stopStyle(Style.HEADER);
		return this;
	}

	public ForumMessageBuilder stopItalic()
	{
		this.stopStyle(Style.ITALIC);
		return this;
	}

	public ForumMessageBuilder stopQuote()
	{
		this.stopStyle(Style.QUOTE);
		return this;
	}

	public ForumMessageBuilder stopSpoiler()
	{
		this.stopStyle(Style.SPOILER);
		return this;
	}

	public ForumMessageBuilder stopStrike()
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

	public ForumMessageBuilder stopURL()
	{
		this.stopStyle(Style.BOLD);
		return this;
	}
}
