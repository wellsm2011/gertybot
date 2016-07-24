package werewolf.experimental;

import java.util.HashMap;
import java.util.Map;

import werewolf.net.Message.Style;
import werewolf.net.MessageEncoder;

public class HTMLMessageEncoder extends MessageEncoder
{

	private static final String SPOILER_START =
			//@formatter:off
			"<style type=\"text/css\">"
				+ ".spoilerbutton {display:block;margin:5px 0;} "
				+ ".spoiler {overflow:hidden;background: #f5f5f5;} "
				+ ".spoilerbutton[value=\"Show\"] + .spoiler > div {margin-top:-100%%;} "
				+ ".spoilerbutton[value=\"Hide\"] + .spoiler {padding:5px;}"
			+ "</style> "
			+ "<input class=\"spoilerbutton\" type=\"button\" value=\"Show\" onclick=\"this.value=this.value=='Show'?'Hide':'Show';\">"
			+ "<div class=\"spoiler\"><div>";
			//@formatter:on

	private Map<Style, Tags> map;

	public HTMLMessageEncoder()
	{
		this.map = new HashMap<>();
		this.map.put(Style.QUOTE, new Tags("<div><h2>%s</h2><div>", "</div></div>"));
		this.map.put(Style.SPOILER, new Tags(HTMLMessageEncoder.SPOILER_START, "</div></div>"));
		this.map.put(Style.STRIKE, new Tags("<s>", "</s>"));
		this.map.put(Style.URL, new Tags("<a href=\"%s\">", "</a>"));
		this.map.put(Style.BOLD, new Tags("<b>", "</b>"));
		this.map.put(Style.ITALIC, new Tags("<i>", "</i>"));
		this.map.put(Style.COLOR, new Tags("<div style=\"color:#%s\">", "</div>"));
		this.map.put(Style.HEADER, new Tags("<h2>", "<h2>"));
		this.map.put(Style.UNDERLINE, new Tags("<u>", "</u>"));
		this.map.put(Style.CODE, new Tags("<code>", "</code>"));
		this.map.put(Style.LIST, new Tags("<ul>", "</ul>"));
		this.map.put(Style.LISTITEM, new Tags("<li>", "</li>"));
	}

	@Override
	public String escape(String msg)
	{
		return msg.replaceAll("\n", "<br>");
	}

	@Override
	protected Map<Style, Tags> getTagMap()
	{
		return this.map;
	}

}
