package werewolf;

/**
 * A simple pair of things.
 * 
 * @author Andrew Binns
 * @param <A>
 *            the first type
 * @param <B>
 *            the second type
 */
public class Pair<A, B>
{
	private A	a;
	private B	b;

	/**
	 * Initializes the pair with two objects
	 * 
	 * @param a
	 *            the first item
	 * @param b
	 *            the second item
	 */
	public Pair(A a, B b)
	{
		this.a = a;
		this.b = b;
	}

	/**
	 * @return the first item in the pair
	 */
	public A getA()
	{
		return this.a;
	}

	/**
	 * @return the second item in the pair
	 */
	public B getB()
	{
		return this.b;
	}

}
