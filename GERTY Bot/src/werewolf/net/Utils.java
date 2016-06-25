package werewolf.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import werewolf.log.WerewolfLogger;
import werewolf.net.neon.NeonContext;

public final class Utils
{
	public static class RomanNumerals
	{
		public enum Numeral
		{
			M(1000), CM(900), D(500), CD(400), C(100), XC(90), L(50), XL(40), X(10), IX(9), V(5), IV(4), I(1);

			private static final Set<Numeral>	SET	= Collections.unmodifiableSet(EnumSet.allOf(Numeral.class));

			public static Numeral getLargest(long weight)
			{
				return Numeral.SET.stream().filter(numeral -> weight >= numeral.weight).findFirst().orElse(I);
			}

			public final long	weight;

			private Numeral(long weight)
			{
				this.weight = weight;
			}
		};

		public static int decode(String roman)
		{
			long result = new StringBuilder(roman.toUpperCase()).reverse().chars().mapToObj(c -> Character.toString((char) c)).map(numeral -> Enum.valueOf(Numeral.class, numeral))
					.mapToLong(numeral -> numeral.weight).reduce(0, (a, b) -> a + (a <= b ? b : -b));
			if (roman.length() > 1 && roman.charAt(0) == roman.charAt(1))
				result += 2 * Enum.valueOf(Numeral.class, roman.substring(0, 1)).weight;
			return (int) result;
		}

		public static String encode(int n)
		{
			return LongStream.iterate(n, l -> l - Numeral.getLargest(l).weight).limit(Numeral.values().length).filter(l -> l > 0).mapToObj(Numeral::getLargest).map(String::valueOf)
					.collect(Collectors.joining());
		}
	}

	public static final LinkedList<ForumContext>	CONTEXTS		= new LinkedList<ForumContext>();

	private static Properties						loginInfo		= null;
	// Minutes between scans.
	public static final long						pollInterval	= 2;
	public static final boolean						parseAllThreads	= false;

	public static String getProperty(String key)
	{
		try
		{
			if (Utils.loginInfo == null)
				Utils.readProperties();
			return Utils.loginInfo.getProperty(key);
		} catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String... cheese) throws IOException, InterruptedException
	{
		WerewolfLogger.setup();

		// Start each context the bot should run in.
		new Thread(NeonContext.INSTANCE).start();
		// new Thread(HalolzContext.INSTANCE).start();
	}

	private static void readProperties() throws IOException
	{
		Utils.loginInfo = new Properties();
		try
		{
			FileInputStream in = new FileInputStream("./local.properties");
			Utils.loginInfo.load(in);
			in.close();
		} catch (FileNotFoundException ex)
		{
			System.err.println("Could not load login data for forums.");
			throw new IOException(ex);
		}
	}
}
