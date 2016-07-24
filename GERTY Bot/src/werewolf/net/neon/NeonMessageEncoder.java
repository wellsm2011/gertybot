package werewolf.net.neon;

import java.util.HashMap;
import java.util.Map;

import werewolf.net.MessageEncoder;
import werewolf.net.Message.Style;

public class NeonMessageEncoder extends MessageEncoder
{
	private Map<Style, Tags> map;

	public NeonMessageEncoder()
	{
		this.map = new HashMap<>();
		this.map.put(Style.QUOTE, new Tags("[quote=%s]", "[/quote]"));
		this.map.put(Style.SPOILER, new Tags("[size=200]%s[/size][spoiler]", "[/spoiler]"));
		this.map.put(Style.STRIKE, new Tags("[s]", "[/s]"));
		this.map.put(Style.URL, new Tags("[url=%s]", "[/url]"));
		this.map.put(Style.BOLD, new Tags("[b]", "[/b]"));
		this.map.put(Style.ITALIC, new Tags("[i]", "[/i]"));
		this.map.put(Style.COLOR, new Tags("[color=#%s]", "[/color]"));
		this.map.put(Style.HEADER, new Tags("[size=200]", "[/size]"));
		this.map.put(Style.UNDERLINE, new Tags("", ""));
		this.map.put(Style.CODE, new Tags("[code]", "[/code]"));
		/*
		 * FIXME make sure this is the correct NEON format for lists. Not my job
		 * =p
		 */
		this.map.put(Style.LIST, new Tags("[list]", "[/list]"));
		this.map.put(Style.LISTITEM, new Tags("[*]", ""));
	}

	@Override
	public String escape(String msg)
	{
		return msg;
	}

	@Override
	protected Map<Style, Tags> getTagMap()
	{
		return this.map;
	}

}
