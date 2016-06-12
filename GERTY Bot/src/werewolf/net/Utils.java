package werewolf.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import werewolf.log.WerewolfLogger;
import werewolf.net.halolz.HalolzContext;
import werewolf.net.neon.NeonContext;

public final class Utils {
	public static final LinkedList<ForumContext> CONTEXTS = new LinkedList<ForumContext>();
	private static Properties loginInfo = null;

	public static final long pollInterval = 2; // Minutes between scans.
	public static final boolean parseAllThreads = false;

	public static void main(String... cheese) throws IOException,
			InterruptedException {
		WerewolfLogger.setup();

		// Start each context the bot should run in.
		new Thread(NeonContext.INSTANCE).start();
		// new Thread(HalolzContext.INSTANCE).start();
	}

	private static void readProperties() throws IOException {
		Properties applicationProps = new Properties();
		FileInputStream in = new FileInputStream("appProperties");
		applicationProps.load(in);
		in.close();
	}

	public static String getProperty(String key) {
		try {
			if (loginInfo == null)
				readProperties();
			return loginInfo.getProperty(key);
		} catch (IOException ex) {
			return null;
		}
	}

	public static class RomanNumerals {
		public enum Numeral {
			M(1000), CM(900), D(500), CD(400), C(100), XC(90), L(50), XL(40), X(
					10), IX(9), V(5), IV(4), I(1);

			public final long weight;

			private static final Set<Numeral> SET = Collections
					.unmodifiableSet(EnumSet.allOf(Numeral.class));

			private Numeral(long weight) {
				this.weight = weight;
			}

			public static Numeral getLargest(long weight) {
				return SET.stream().filter(numeral -> weight >= numeral.weight)
						.findFirst().orElse(I);
			}
		};

		public static String encode(int n) {
			return LongStream.iterate(n, l -> l - Numeral.getLargest(l).weight)
					.limit(Numeral.values().length).filter(l -> l > 0)
					.mapToObj(Numeral::getLargest).map(String::valueOf)
					.collect(Collectors.joining());
		}

		public static int decode(String roman) {
			long result = new StringBuilder(roman.toUpperCase())
					.reverse()
					.chars()
					.mapToObj(c -> Character.toString((char) c))
					.map(numeral -> Enum.valueOf(Numeral.class,
							(String) numeral))
					.mapToLong(numeral -> numeral.weight)
					.reduce(0, (a, b) -> a + (a <= b ? b : -b));
			if (roman.length() > 1 && roman.charAt(0) == roman.charAt(1))
				result += 2 * Enum
						.valueOf(Numeral.class, roman.substring(0, 1)).weight;
			return (int) result;
		}
	}

	public static int sum(int... ary) {
		int sum = 0;
		for (int i : ary)
			sum += i;
		return sum;
	}

	public static double sum(double... ary) {
		double sum = 0;
		for (double i : ary)
			sum += i;
		return sum;
	}

	public static int[] sum(int[]... ary) {
		int length = 0;
		for (int[] i : ary)
			length = Math.max(i.length, length);
		int[] sum = new int[length];

		for (int[] i : ary) {
			for (int j = 0; j < i.length; ++j)
				sum[j] += i[j];
		}
		return sum;
	}

	public static double[] sum(double[]... ary) {
		int length = 0;
		for (double[] i : ary)
			length = Math.max(i.length, length);
		double[] sum = new double[length];

		for (double[] i : ary) {
			for (int j = 0; j < i.length; ++j)
				sum[j] += i[j];
		}
		return sum;
	}
}
