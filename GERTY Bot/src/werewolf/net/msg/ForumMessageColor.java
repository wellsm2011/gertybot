package werewolf.net.msg;

import java.awt.Color;

public class ForumMessageColor extends ForumMessageElement
{
	// Default color for names to be posted in.
	public static final Color	VILLAGE		= Color.GREEN;
	public static final Color	EVIL		= Color.RED;
	public static final Color	NEUTRAL		= Color.YELLOW;
	public static final Color	THIRD_PARTY	= new Color(0xFF2DC3);
	public static final Color	DEAD		= new Color(0xFFBF80);
	public static final Color	INVALID		= Color.RED;

	private Color				color;

	public ForumMessageColor(Color color, String msg)
	{
		this(color, new ForumMessageString(msg));
	}

	public ForumMessageColor(Color color, ForumMessageElement... children)
	{
		super(children);
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}
}
