package werewolf.game;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Manages and tracks all of the votes in a single Werewolf game.
 * 
 * @author Michael
 */
public class VoteManager
{
	private class RoundRecord
	{

	}

	private final static Logger		LOGGER	= Logger.getLogger(VoteManager.class.getName());
	private WerewolfGame			game;
	private LinkedList<RoundRecord>	record = new LinkedList<>();

	public VoteManager(WerewolfGame game)
	{
		this.game = game;
	}
}
