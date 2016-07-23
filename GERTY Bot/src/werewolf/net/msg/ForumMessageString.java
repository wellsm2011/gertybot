package werewolf.net.msg;

import werewolf.net.ForumMessageEncoder;

public class ForumMessageString extends ForumMessageElement
{
	private String	msg	= "";

	public ForumMessageString(String msg)
	{
		this.msg = msg;
	}

	@Override
	public ForumMessageString append(ForumMessageElement elm)
	{
		return this.append(ForumMessageEncoder.PLAINTEXT.encodeMessage(elm));
	}

	@Override
	public ForumMessageString append(String msg)
	{
		// FIXME this is most likely a silly derp, and doesn't work. Just FYI. -
		// Sudo
		this.msg += msg;
		return this;
	}

	public String getMsg()
	{
		return this.msg;
	}
}
