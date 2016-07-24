package werewolf.game.voteconfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import werewolf.Pair;
import werewolf.game.Player;
import werewolf.game.User;
import werewolf.game.Vote;

/**
 * Records all of the votes that happen in a given round.
 * 
 * @author Michael
 * @author Andrew
 */
public class RoundRecord implements Serializable
{
	private static final long serialVersionUID = -2976634157210100218L;
	/*
	 * By default, display the last vote made by each player and lynch everyone
	 * in a tie.
	 */
	private RoundRecord			previous;
	private List<Vote>			record	= new ArrayList<>();
	private VoteConfiguration	config;

	public RoundRecord()
	{
		this(VoteConfiguration.getDefault());
	}

	public RoundRecord(RoundRecord previous)
	{
		this(previous.config.copy(), previous);
	}

	public RoundRecord(VoteConfiguration config)
	{
		this(config, null);
	}

	public RoundRecord(VoteConfiguration config, RoundRecord previous)
	{
		this.previous = previous;
		this.config = config;
	}

	public List<Vote> getRecord()
	{
		return Collections.unmodifiableList(this.record);
	}

	public ListIterator<Vote> getReverseIterator()
	{
		return this.getRecord().listIterator(this.record.size());
	}

	public int getRound()
	{
		if (this.previous == null)
			return 1;
		return this.previous.getRound() + 1;
	}

	public Map<Pair<User, Integer>, Set<Player>> getUserTally()
	{
		/**
		 * Map of players and counts to sets of voters. This is sorted based off
		 * the type of user, the user, and the count.
		 */
		Map<Pair<User, Integer>, Set<Player>> perUserTally = new TreeMap<>((a, b) -> {
			if (a.getA() instanceof Player && !(b.getA() instanceof Player))
				return -1;
			if (!(a.getA() instanceof Player) && b.getA() instanceof Player)
				return 1;
			if (a.getA() instanceof Player)
				return a.getB().compareTo(b.getB());
			return a.getA().compareTo(b.getA());
		});
		/*
		 * Loop through all individual users who have active votes in the
		 * current record of votes
		 */
		for (User u : this.record.stream().map(v -> v.getVoter()).distinct().collect(Collectors.toList()))
		{
			/*
			 * Count the number of votes for this user, and add that to a pair
			 * which is used as a key for the map. The map is sorted by the
			 * number of votes, as well as whether the users are static or
			 * player users.
			 */
			Pair<User, Integer> key = new Pair<>(u, this.getVotesTargetedAt(u).mapToInt(this.config.getVoteValueMapping(this)).sum());
			/*
			 * Generates a collection of all of the users who voted for this
			 * user, in order of their votes.
			 */
			Set<Player> voters = this.getVotesTargetedAt(u).map(v -> v.getVoter()).collect(Collectors.toCollection(LinkedHashSet::new));
			perUserTally.put(key, voters);
		}
		return perUserTally;
	}

	/**
	 * For a given user, provides a stream of Votes that are targeted at the
	 * given user. Filters by {@link #displayFilter} as well.
	 * 
	 * @param u
	 *            the user to find votes for
	 * @return a stream of display-able votes targeted at the user in question
	 */
	private Stream<Vote> getVotesTargetedAt(User u)
	{
		return this.record.stream().filter(v -> v.getTarget().equals(u)).filter(this.config.getDisplayFilter(this));
	}

	public RoundRecord previous()
	{
		return this.previous;
	}

	public void record(Vote vote)
	{
		this.record.add(vote);
	}
}