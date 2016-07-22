package werewolf;

import java.util.logging.Logger;

import werewolf.log.WerewolfLogger;
import werewolf.net.ForumContext;
import werewolf.net.neon.NeonContext;

public class Driver
{
	private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());

	public static void launchContext(String context) throws Exception
	{
		Driver.LOGGER.info("Loading context: " + context);
		String className = "werewolf.net." + context.toLowerCase() + "." + context + "Context";
		ForumContext obj = (NeonContext) Class.forName(className).getField("INSTANCE").get(null);
		new Thread(obj).start();
	}

	public static void main(String... cheese) throws Exception
	{
		WerewolfLogger.setup();

		String[] contexts = Utils.getProperty("contexts", "").split(",");

		if (contexts.length == 0)
			throw new IllegalArgumentException("No contexts to start. Please configure local properties.");

		for (String context : contexts)
			Driver.launchContext(context);
	}
}
