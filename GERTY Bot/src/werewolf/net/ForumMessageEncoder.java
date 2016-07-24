package werewolf.net;

import java.util.HashMap;
import java.util.Map;

import werewolf.net.Message.Style;

public abstract class ForumMessageEncoder
{
	protected class Tags
	{
		private String	opening;
		private String	closing;

		public Tags(String opening, String closing)
		{
			this.opening = opening;
			this.closing = closing;
		}

		public String getClosing()
		{
			return this.closing;
		}

		public String getOpening()
		{
			return this.opening;
		}
	}

	public static final ForumMessageEncoder PLAINTEXT = new ForumMessageEncoder()
	{
		private Map<Style, Tags> map;

		@Override
		public String escape(String msg)
		{
			return msg;
		}

		@Override
		protected Map<Style, Tags> getTagMap()
		{
			if (this.map == null)
				this.initMap();
			return this.map;
		}

		private void initMap()
		{
			this.map = new HashMap<>();
			this.map.put(Style.QUOTE, new Tags("Quote (%s):\n", ""));
			this.map.put(Style.SPOILER, new Tags("[[%s]]:\n", ""));
			this.map.put(Style.STRIKE, new Tags("", ""));
			this.map.put(Style.URL, new Tags("", "<%s>"));
			this.map.put(Style.BOLD, new Tags("", ""));
			this.map.put(Style.ITALIC, new Tags("", ""));
			this.map.put(Style.COLOR, new Tags("", ""));
			this.map.put(Style.HEADER, new Tags("\n\n", "\n\n"));
			this.map.put(Style.UNDERLINE, new Tags("", ""));
			this.map.put(Style.CODE, new Tags("\n", "\n"));
			this.map.put(Style.LIST, new Tags("--------", "--------"));
			this.map.put(Style.LISTITEM, new Tags("(O)", "\n"));
		}
	};

	public static final ForumMessageEncoder DEBUG = new ForumMessageEncoder()
	{
		private Map<Style, Tags> map;

		@Override
		public String escape(String msg)
		{
			return msg;
		}

		@Override
		protected Map<Style, Tags> getTagMap()
		{
			if (this.map == null)
				this.initMap();
			return this.map;
		}

		private void initMap()
		{
			this.map = new HashMap<>();
			this.map.put(Style.QUOTE, new Tags("<START_QUOTE[%s]>", "<STOP_QUOTE>"));
			this.map.put(Style.SPOILER, new Tags("<START_SPOILER[%s]>", "<STOP_SPOILER>"));
			this.map.put(Style.STRIKE, new Tags("<START_STRIKE>", "<STOP_STRIKE>"));
			this.map.put(Style.URL, new Tags("<START_URL[%s]>", "<STOP_URL>"));
			this.map.put(Style.BOLD, new Tags("<START_BOLD>", "<STOP_BOLD>"));
			this.map.put(Style.ITALIC, new Tags("<START_ITALIC>", "<STOP_ITALIC>"));
			this.map.put(Style.COLOR, new Tags("<START_COLOR[%s]>", "STOP_COLOR"));
			this.map.put(Style.HEADER, new Tags("START_HEADER", "STOP_HEADER"));
			this.map.put(Style.UNDERLINE, new Tags("START_UNDERLINE", "STOP_UNDERLINE"));
			this.map.put(Style.CODE, new Tags("START_CODE", "STOP_CODE"));
			this.map.put(Style.LIST, new Tags("STOP_LIST", "START_LIST"));
			this.map.put(Style.LISTITEM, new Tags("START_LISTITEM", "STOP_LISTITEM"));
		}
	};

	public abstract String escape(String msg);

	public String getClosing(Style style, Object... in)
	{
		String tag = this.getTagMap().get(style).getClosing();
		return String.format(tag, in);
	}

	public String getOpening(Style style, Object... in)
	{
		String tag = this.getTagMap().get(style).getOpening();
		return String.format(tag, in);
	}

	protected abstract Map<Style, Tags> getTagMap();
}
