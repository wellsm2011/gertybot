package werewolf.net;

import java.awt.Color;

public abstract class ForumMessageEncoder
{
	

	public abstract String encodeBold(String msg);

	public abstract String encodeItalic(String msg);

	public abstract String encodeStrike(String msg);

	public abstract String encodeHeader(String msg);

	public abstract String encodeColor(String msg, Color color);

	public abstract String encodeUrl(String msg, String url);
}
