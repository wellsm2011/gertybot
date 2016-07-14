package werewolf.net.neon;

import java.awt.Color;

import werewolf.net.ForumMessageEncoder;

public class NeonMessageEncoder extends ForumMessageEncoder
{
	@Override
	protected String encodeBold(String msg)
	{
		return "[b]" + msg + "[/b]";
	}

	@Override
	protected String encodeCodeblock(String msg)
	{
		return "[code]" + msg + "[/code]";
	}

	@Override
	protected String encodeColor(String msg, Color color)
	{
		String rgb = Integer.toString(color.getRGB() % 0xFFFFFF, 16);
		return "[color=#" + rgb + "]" + msg + "[/color]";
	}

	@Override
	protected String encodeHeader(String msg)
	{
		return "[size=200]" + msg + "[/size]";
	}

	@Override
	protected String encodeItalic(String msg)
	{
		return "[i]" + msg + "[/i]";
	}

	@Override
	protected String encodeQuote(String msg, String author)
	{
		return "[quote=" + author + "]" + msg + "[/quote]";
	}

	@Override
	protected String encodeSpoiler(String msg, String title)
	{
		return this.encodeHeader(title) + "[spoiler]" + msg + "[/spoiler]";
	}

	@Override
	protected String encodeStrike(String msg)
	{
		return "[s]" + msg + "[/s]";
	}

	@Override
	protected String encodeUrl(String msg, String url)
	{
		return "[url=" + url + "]" + msg + "[/url]";
	}

	@Override
	protected String escape(String msg)
	{
		return msg;
	}

}
