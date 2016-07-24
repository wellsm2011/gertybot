package werewolf.experimental;

import java.util.HashMap;
import java.util.Map;

import werewolf.net.Message;
import werewolf.net.Message.Style;
import werewolf.net.MessageEncoder;

public class HTMLMessageEncoder extends MessageEncoder
{
	private Map<Style, Tags> map;

	@Override
	public String escape(String msg)
	{
		return msg.replaceAll("\n", "<br>");
	}

	@Override
	protected Map<Style, Tags> getTagMap()
	{
		if (this.map == null)
			this.initMap();
		return this.map;
	}

	/**
	 * Seperated off due to long length. This uses a nifty little CSS trick to
	 * get around using ID's to show/hide an element, resulting in a clean, and
	 * portable spoiler result. The css could be centralized if there was desire
	 * eventually, but doesn't really make sense if we want the
	 * {@link Message#formatString(MessageEncoder)} to work exactly as
	 * it does currently.
	 */
	private static final String SPOILER_START =
			//@formatter:off
			"<h2>%s</h2>"
			+ "<style type=\"text/css\">"
				+ ".spoilerbutton {display:block;margin:5px 0;} "
				+ ".spoiler {overflow:hidden;background: #f5f5f5;} "
				+ ".spoilerbutton[value=\"Show\"] + .spoiler > div {margin-top:-100%%;}"
				+ " .spoilerbutton[value=\"Hide\"] + .spoiler {padding:5px;}</style> "
			+ "<input class=\"spoilerbutton\" type=\"button\" value=\"Show\" onclick=\"this.value=this.value=='Show'?'Hide':'Show';\">"
			+ "<div class=\"spoiler\"><div>";
			//@formatter:on

	private void initMap()
	{
		this.map = new HashMap<>();
		this.map.put(Style.QUOTE, new Tags("<div class=\"quote\"><h2>%s:<h2><div class=\"quotebody\" style=\"background:#f5f5f5\">", "</div></div>"));
		this.map.put(Style.SPOILER, new Tags(SPOILER_START, "</div></div>"));
		this.map.put(Style.STRIKE, new Tags("<s>", "</s>"));
		this.map.put(Style.URL, new Tags("<a href=\"%s\">", "</a>"));
		this.map.put(Style.BOLD, new Tags("<b>", "</b>"));
		this.map.put(Style.ITALIC, new Tags("<i>", "</i>"));
		this.map.put(Style.COLOR, new Tags("<div style=\"color:#%s\">", "</div>"));
		this.map.put(Style.HEADER, new Tags("<h2>", "</h2>"));
		this.map.put(Style.UNDERLINE, new Tags("<u>", "</u>"));
		this.map.put(Style.CODE, new Tags("<code>", "</code>"));
		this.map.put(Style.LIST, new Tags("<ul>", "</ul>"));
		this.map.put(Style.LISTITEM, new Tags("<li>", "</li>"));
	}
}
