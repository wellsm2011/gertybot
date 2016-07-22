package werewolf.game;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * Manages and tracks all of the votes in a single Werewolf game.
 * 
 * @author Michael
 */
public class VoteManager implements java.io.Serializable
{
	/**
	 * Records all of the votes that happen in a given round.
	 * 
	 * @author Michael
	 */
	public class RoundRecord implements java.io.Serializable
	{
		private RoundRecord									previous;
		LinkedList<Vote>									record	= new LinkedList<>();
		private BiFunction<RoundRecord, WerewolfGame, User>	lynchResolver;
		private BiFunction<Player, Player, Integer>			voteApplier;

		public RoundRecord()
		{
			this.previous = null;
		}

		public RoundRecord(RoundRecord previous)
		{
			this.previous = previous;
		}

		public List<Vote> getRecord()
		{
			return this.record;
		}

		public RoundRecord previous()
		{
			return this.previous;
		}

		public void record(Vote vote)
		{
			this.record.add(vote);
		}

		public BiFunction<RoundRecord, WerewolfGame, User> getLynchResolver()
		{
			return lynchResolver;
		}

		public void setLynchResolver(BiFunction<RoundRecord, WerewolfGame, User> lynchResolver)
		{
			this.lynchResolver = lynchResolver;
		}
	}

	private final static Logger	LOGGER	= Logger.getLogger(VoteManager.class.getName());
	private RoundRecord			record	= new RoundRecord();
	private WerewolfGame		game;

	public VoteManager(WerewolfGame game)
	{
		this.game = game;
	}

	public void endRound()
	{
		this.record = new RoundRecord(this.record);
	}
}
