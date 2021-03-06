import werewolf.experimental.HTMLMessageEncoder;
import werewolf.net.Message;
import werewolf.net.MessageEncoder;
import werewolf.net.neon.NeonMessageEncoder;

public class MessageBuilderTest
{
	public static void main(String[] args)
	{
		Message b = new Message();

		b.add("normal text").startBold().add(" bolded ").startItalic().add("and normal").stopBold();
		b.add("\nThis should be normal, after the bold and italc were stopped");
		b.add("\nand now for...").startURL("http://google.com").add("some text in a url!");
		b.startBold().add("that is bolded!").stopBold().add("in only certain places").stopURL();
		b.add("with only the correct bits of the url being stopped.\n");

		System.out.println(b.formatString(MessageEncoder.DEBUG));
		System.out.println(b.formatString(MessageEncoder.PLAINTEXT));
		System.out.println(b.formatString(new NeonMessageEncoder()));
		System.out.println(b.formatString(new HTMLMessageEncoder()));

	}
}
