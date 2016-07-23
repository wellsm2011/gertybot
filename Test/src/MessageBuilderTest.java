import werewolf.experimental.ForumMessageBuilder;
import werewolf.experimental.ForumMessageEncoder;
import werewolf.experimental.NeonMessageEncoder;

public class MessageBuilderTest
{
	public static void main(String[] args)
	{
		ForumMessageBuilder b = new ForumMessageBuilder();

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
