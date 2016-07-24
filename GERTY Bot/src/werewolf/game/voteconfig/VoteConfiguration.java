package werewolf.game.voteconfig;

import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import werewolf.game.Vote;

public class VoteConfiguration
{
	protected static final Function<RoundRecord, Predicate<? super Vote>> DEFAULT_DISPLAY_FILTER = rec -> {
		return v -> {
			ListIterator<Vote> iter = rec.getReverseIterator();
			Vote cur = iter.previous();
			while (iter.hasNext())
				if (v.getVoter().equals(cur.getVoter()))
					return v.equals(cur);
			return false;
		};
	};

	protected static final Function<RoundRecord, ToIntFunction<? super Vote>> DEFAULT_VOTE_VALUATOR = rec -> {
		return v -> 1;
	};

	public static VoteConfiguration getDefault()
	{
		return new VoteConfiguration();
	}

	protected Function<RoundRecord, Predicate<? super Vote>> displayFilter;

	protected Function<RoundRecord, ToIntFunction<? super Vote>> voteValuation;

	public VoteConfiguration()
	{
		this(VoteConfiguration.DEFAULT_DISPLAY_FILTER, VoteConfiguration.DEFAULT_VOTE_VALUATOR);
	}

	public VoteConfiguration(Function<RoundRecord, Predicate<? super Vote>> inputDisplayFilter, Function<RoundRecord, ToIntFunction<? super Vote>> inputVoteValuator)
	{
		this.displayFilter = inputDisplayFilter;
		this.voteValuation = inputVoteValuator;
	}

	public VoteConfiguration(VoteConfiguration other)
	{
		this.displayFilter = other.displayFilter;
		this.voteValuation = other.voteValuation;
	}

	public VoteConfiguration copy()
	{
		return new VoteConfiguration(this);
	}

	/**
	 * @param rec
	 *            The record of votes to filter on.
	 * @return A Predicate which returns true if a given vote should be
	 *         displayed.
	 */
	public Predicate<? super Vote> getDisplayFilter(RoundRecord rec)
	{
		return this.displayFilter.apply(rec);
	}

	/**
	 * @param rec
	 *            The record of votes to map.
	 * @return A function which returns the power of a given vote.
	 */
	public ToIntFunction<? super Vote> getVoteValueMapping(RoundRecord rec)
	{
		return this.voteValuation.apply(rec);
	}

	public void setDisplayFilter(Function<RoundRecord, Predicate<? super Vote>> newFilter)
	{
		this.displayFilter = newFilter;
	}

}
