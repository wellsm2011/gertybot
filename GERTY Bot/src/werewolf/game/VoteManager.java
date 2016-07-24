package werewolf.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import werewolf.net.ForumMessage;
import javafx.util.Pair;

/**
 * Manages and tracks all of the votes in a single Werewolf game.
 * 
 * @author Michael
 */
public class VoteManager implements java.io.Serializable
{
	private class PlayerRecord implements java.io.Serializable
	{
		LinkedList<Player>	voters		= new LinkedList<>();
		int					totalPower	= 0;
	}

	/**
	 * Records all of the votes that happen in a given round.
	 * 
	 * @author Michael
	 */
	public class RoundRecord implements java.io.Serializable
	{
		private static final long									serialVersionUID	= -2976634157210100218L;
		/*
		 * By default, accept all votes and always return all players in a tie.
		 */
		private Function<RoundRecord, List<Vote>>					voteValidator		= (rr) -> rr.getRecord();
		private BiFunction<RoundRecord, List<Player>, List<Player>>	tieResolver			= (rr, p) -> p;
		private RoundRecord											previous;
		private LinkedList<Vote>									record				= new LinkedList<>();

		public RoundRecord()
		{
			this.previous = null;
		}

		public RoundRecord(RoundRecord previous)
		{
			this.previous = previous;
			this.voteValidator = previous.voteValidator;
			this.tieResolver = previous.tieResolver;
		}

		public List<Vote> getFullRecord()
		{
			return this.record;
		}

		public List<Vote> getRecord()
		{
			return this.voteValidator.apply(this);
		}

		public int getRound()
		{
			if (this.previous == null)
				return 1;
			return this.previous.getRound() + 1;
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

	private static final long	serialVersionUID	= 5837780729132043518L;

	private final static Logger	LOGGER				= Logger.getLogger(VoteManager.class.getName());

	private RoundRecord			record				= new RoundRecord();
	private WerewolfGame		game;

	public VoteManager(WerewolfGame game)
	{
		this.game = game;
	}

	public void castVote(Vote vote)
	{
		this.record.record.add(vote);
	}

	public void endRound()
	{
		this.record = new RoundRecord(this.record);
	}

	/**
	 * @return the record of all votes this round.
	 */
	public RoundRecord getRecord()
	{
		return this.record;
	}

	/**
	 * @param tieResolver
	 *            the function which resolves which of an array of tied players
	 *            should be lynched. Returns a list of all players who should be
	 *            lynched.
	 */
	public void setTieResolver(BiFunction<RoundRecord, List<Player>, List<Player>> tieResolver)
	{
		this.record.tieResolver = tieResolver;
	}

	/**
	 * @param voteCounter
	 *            The vote validator to set. Takes the record of votes and
	 *            returns a set of valid votes which should be tallied for the
	 *            round.
	 */
	public void setVoteValidator(Function<RoundRecord, List<Vote>> voteCounter)
	{
		this.record.voteValidator = voteCounter;
	}

	public static Map<Pair<User,Integer>,Set<Player>> getUserTally(List<Vote> votes, Predicate<Vote> countFilter){
	Map<Pair<User,Integer>, Set<Player>> perUserTally;
	perUserTally = new TreeMap<>((a,b)->{
	       if(a instanceof Player && !(b instanceOf Player))
	           return -1;
	       if(b instanceof Player && !(a instanceOf Player))
	           return 1;
	       if(a instanceof Player)
	           return a.getB().compareTo(b.getB());
	       return a.compareTo(b);
	   });
   for(User u : votes.stream().map(v->v.getUser()).distinct().collect(Collectors.toList()){
       Pair<User,Integer> key = new Pair<>(u,(int)votes.stream().filter(v->v.getTarget().equals(u)).filter(roundManager.getDisplayFilter()).count());
       Set<Player> voters = votes.stream().filter(v->v.getTarget().equals(u)).filter(roundManager.getDisplayFilter()).map(v->v.getVoter()).collect(Collectors.toCollection(LinkedHashSet::new));
       perUserTally.put(key,voters);
   }
   return preUserTally;
}
}
