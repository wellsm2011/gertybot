package werewolf.net.msg;

public class ForumMessageString extends ForumMessageElement
{
	private String	msg;

	public ForumMessageString(String msg)
	{
		this.msg = msg;
	}

	public String getMsg()
	{
		return msg;
	}
}
