package werewolf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public final class Utils
{
	private static final Logger	LOGGER	= Logger.getLogger(Utils.class.getName());

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

	private static Properties	localProperties	= null;

	/**
	 * Fetches a property from the first of: local.properties, setup.properties,
	 * default System properties.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static <T> T getProperty(String key, T defaultValue)
	{
		try
		{
			String value = getProperty(key);
			if (value == null || value.length() == 0)
				return defaultValue;
			// Check primitive types to see if we're parsing.
			if (defaultValue instanceof Character)
			{
				if (value.length() != 1)
					return defaultValue;
				return cleanCast(value.charAt(0));
			}
			if (defaultValue instanceof Byte)
				return cleanCast(Byte.valueOf(value));
			if (defaultValue instanceof Short)
				return cleanCast(Short.valueOf(value));
			if (defaultValue instanceof Integer)
				return cleanCast(Integer.valueOf(value));
			if (defaultValue instanceof Long)
				return cleanCast(Long.valueOf(value));
			if (defaultValue instanceof Float)
				return cleanCast(Float.valueOf(value));
			if (defaultValue instanceof Double)
				return cleanCast(Double.valueOf(value));
			if (defaultValue instanceof Boolean)
				return cleanCast(Boolean.valueOf(value));
			// Try to extract a constructor that takes a string and create a new
			// instance of the class.
			return cleanCast(defaultValue.getClass().getConstructor(String.class).newInstance(value));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
		{
			LOGGER.log(Level.WARNING, "Could not construct a new instance of type " + defaultValue.getClass().getName() + ".", ex);
			return defaultValue;
		} catch (IOException ex)
		{
			LOGGER.log(Level.WARNING, "Could not read properties for field " + key + ".", ex);
			return defaultValue;
		}
	}

	public static String getProperty(String key) throws IOException
	{
		if (Utils.localProperties == null)
			Utils.readProperties();
		return Utils.localProperties.getProperty(key);
	}

	public static <T> T getProperty(String key, String defaultValue, Function<String, T> resolver)
	{
		return resolver.apply(getProperty(key, defaultValue));
	}

	@SuppressWarnings("unchecked")
	public static <T> T cleanCast(Object cur)
	{
		return (T) cur;
	}

	private static void readProperties() throws IOException
	{
		Properties setupProperties = new Properties(System.getProperties());
		Utils.localProperties = new Properties(setupProperties);
		try
		{
			FileInputStream in = new FileInputStream("./local.properties");
			Utils.localProperties.load(in);
			in.close();
		} catch (FileNotFoundException ex)
		{
			LOGGER.warning("Could not load ./local.properties.");
			throw new IOException(ex);
		}
	}
}
