import werewolf.net.ForumMessage;
import werewolf.net.ForumMessageEncoder;
import werewolf.net.neon.NeonMessageEncoder;

public class MessageBuilderTest
{
	public static void main(String[] args)
	{
		ForumMessage b = new ForumMessage();

		b.add("normal text").startBold().add(" bolded ").startItalic().add("and normal").stopBold();
		b.add("\nThis should be normal, after the bold and italc were stopped");
		b.add("\nand now for...").startURL("http://google.com").add("some text in a url!");
		b.startBold().add("that is bolded!").stopBold().add("in only certain places").stopURL();
		b.add("with only the correct bits of the url being stopped.\n");

		System.out.println(b.formatString(ForumMessageEncoder.DEBUG));
		System.out.println(b.formatString(ForumMessageEncoder.PLAINTEXT));
		System.out.println(b.formatString(new NeonMessageEncoder()));

	}
}
