package werewolf.net.msg;

import java.util.function.Function;

public abstract class ForumMessageElement
{	
	public String toString(Function<ForumMessageElement, String> encoder) {
		return encoder.apply(this);
	}
}
