package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

public interface ForumPostEditor extends Serializable
{
	public void appendText(String newText) throws IOException;

	public ForumContext getContext();

	public String getReason() throws IOException;

	public String getSubject() throws IOException;

	public String getText() throws IOException;

	public boolean isLocked() throws IOException;

	public void setLocked(boolean locked) throws IOException;

	public void setReason(String newReason) throws IOException;

	public void setSubject(String newSubject) throws IOException;

	public void setText(String newText) throws IOException;

	public void submit() throws IOException;
}
