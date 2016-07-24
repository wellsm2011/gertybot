package werewolf.game;

import java.util.function.Consumer;

public class GamePhase
{
	public static final GamePhase	DAY		= new GamePhase("Day", (WerewolfGame game) -> {

											});
	public static final GamePhase	NIGHT	= new GamePhase("Night", (WerewolfGame game) -> {

											});
	public static final GamePhase	PREGAME	= new GamePhase("Pregame Setup", (WerewolfGame game) -> {

											});

	private String					name;
	private Consumer<WerewolfGame>	resolver;

	public GamePhase(String name, Consumer<WerewolfGame> resolver)
	{
		this.name = name;
		this.resolver = resolver;
	}

	public void resolve(WerewolfGame game)
	{
		this.resolver.accept(game);
	}
}
