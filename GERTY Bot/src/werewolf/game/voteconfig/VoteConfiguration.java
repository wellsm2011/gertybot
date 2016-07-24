package werewolf.game.voteconfig;

import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import werewolf.game.Vote;

public abstract class VoteConfiguration
{

	public static VoteConfiguration getDefault()
	{
		return new VoteConfiguration()
		{

			@Override
			public VoteConfiguration copy()
			{
				// Since we're not state-ful in any way...
				return this;
			}

			@Override
			public Predicate<? super Vote> getDisplayFilter(RoundRecord rec)
			{
				return v -> {
					ListIterator<Vote> iter = rec.getReverseIterator();
					Vote cur = iter.previous();
					while (iter.hasNext())
						if (v.getVoter().equals(cur.getVoter()))
							return v.equals(cur);
					return false;
				};
			}

			@Override
			public ToIntFunction<? super Vote> getVoteValueMapping(RoundRecord rec)
			{
				return v -> 1;
			}

		};
	}

	public abstract VoteConfiguration copy();

	/**
	 * @param rec
	 *            The record of votes to filter on.
	 * @return A Predicate which returns true if a given vote should be
	 *         displayed.
	 */
	public abstract Predicate<? super Vote> getDisplayFilter(RoundRecord rec);

	public abstract ToIntFunction<? super Vote> getVoteValueMapping(RoundRecord rec);

}
