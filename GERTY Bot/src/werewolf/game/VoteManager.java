package werewolf.game;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Manages and tracks all of the votes in a single Werewolf game.
 * 
 * @author Michael
 */
public class VoteManager
{
	public class RoundRecord
	{
		private RoundRecord	previous;
		LinkedList<Vote>	record	= new LinkedList<>();

		public RoundRecord(RoundRecord previous)
		{
			this.previous = previous;
		}

		public RoundRecord()
		{
			this.previous = null;
		}

		public void record(Vote vote)
		{
			record.add(vote);
		}

		public List<Vote> getRecord()
		{
			return record;
		}

		public RoundRecord previous()
		{
			return previous;
		}
	}

	private final static Logger							LOGGER	= Logger.getLogger(VoteManager.class.getName());
	private RoundRecord									record	= new RoundRecord();
	private WerewolfGame								game;
	private BiFunction<RoundRecord, WerewolfGame, User>	lynchResolver;
	private Function<List<Player>, Integer>				voteCounter;

	public VoteManager(WerewolfGame game)
	{
		this.game = game;
	}

	public void endRound()
	{
		record = new RoundRecord(record);
	}
}
