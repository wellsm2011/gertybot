package werewolf.game;


public class PmCreator
{
	/*
	 * private static int round = 5; private static boolean consoleSend = false;
	 * private static boolean testSend = false; private static ForumContext
	 * context = new NeonContext(4793, "Rainboy", "qwerty12"); private static
	 * String data = ""; private static String data2 = ""; private static String
	 * messageHeader =
	 * "[color=#FF0000][size=85]This is an automated message and may contain inaccurate information and "
	 * +
	 * "bugs. Please check the PM and reply if anything is wrong or confusing.[/size][/color]\n\n"
	 * ; public static class Player { String name; String alignment;
	 * LinkedList<String> functions = new LinkedList<String>();
	 * LinkedList<String> overloaded = new LinkedList<String>(); String active =
	 * ""; String active2 = ""; String target = ""; String target2 = ""; String
	 * upgrade = ""; String upgrade2 = ""; String passive = ""; String passive2
	 * = ""; String swap = ""; String obtain = ""; public Player(String[] data)
	 * { int count = 0; name = data[count++]; alignment = data[count++]; for
	 * (;count < data.length;++count) { if (data[count].equals("-")) continue;
	 * if (data[count].endsWith("O")) overloaded.add(data[count].substring(0,
	 * data[count].length() - 1)); else functions.add(data[count]); } } public
	 * void update(String[] data) { int count = 2; active = data[count++];
	 * target = data[count++]; active2 = data[count++]; target2 = data[count++];
	 * upgrade = data[count++]; upgrade2 = data[count++]; passive =
	 * data[count++]; passive2 = data[count++]; } } public static void
	 * main(String[] args) { LinkedList<Player> players = new
	 * LinkedList<Player>(); LinkedList<Player> spies = new
	 * LinkedList<Player>(); StringBuilder processPm = new StringBuilder();
	 * String[] parsedData = data.split("\\;"); String[] parsedData2 =
	 * data2.split("\\;"); String subject = "Assassins VIII: R" + round +
	 * " Results"; for (int i = 0;i < parsedData.length;++i) { String[]
	 * parsedPlayerData = parsedData[i].split("\\|", -1); if
	 * (parsedPlayerData.length <= 1) continue; Player player = new
	 * Player(parsedPlayerData); player.update(parsedData2[i].split("\\|", -1));
	 * } System.out.println("Discovered Players: " + players.size()); for
	 * (Player plr : players) { if (!plr.isAlive() || plr.hasEscaped())
	 * continue; StringBuilder message = new StringBuilder(); if (plr.isSpy()) {
	 * message = spyPm; message.append(plr.name + "\n[spoiler]"); }
	 * message.append("Name: " + plr.name + "\n"); message.append("Alignment: "
	 * + plr.alignment + "\n"); message.append("Role: " + plr.role + "\n");
	 * message.append("Status: Alive, " + plural(plr.injuries, "injury",
	 * " injuries") + "\n"); message.append("Available AP R" + round + ": " +
	 * plr.ap + "\n"); message.append("Base AP R" + (round + 1) + ": " +
	 * plr.nextAp + "\n"); message.append("Current Location: " + plr.location +
	 * " (" + plr.control + " Control)\n"); if (plr.isSpy())
	 * message.append("Displacement Location: " + plr.lynchLocation + "\n");
	 * message.append("\n\n"); if (plr.foundItems > 0) {
	 * message.append("You found " + plural(plr.foundItems, "item", "items") +
	 * " this round by Searching, Killing or Stealing.\n\n"); } int lostItems =
	 * plr.itemsStolen + plr.usedItems; if (lostItems > 0) {
	 * message.append("You lost " + plural(lostItems, "item", "items") +
	 * " this round. (" + plr.itemsStolen + " stolen, " + plr.usedItems +
	 * " used)\n\n"); } boolean attacked = false; int attacksMade = 0; for
	 * (Attack atk : attacks) { if (atk.attacker.equalsIgnoreCase(plr.name)) {
	 * message.append("You attacked " + atk.defender + (getPlayer(players,
	 * atk.defender).isAlive() ? " but failed to kill them.\n\n" :
	 * " and killed them.\n\n")); attacksMade++; } if
	 * (atk.defender.equalsIgnoreCase(plr.name)) attacked = true; } if
	 * (plr.invisible) message.append(
	 * "You used a Scroll of Moonlight and are invisible to other players this round.\n\n"
	 * ); if (!attacked)
	 * message.append("You were not directly attacked this round.\n\n"); else if
	 * (plr.ironSword < 1 || plr.attacked >= plr.defense)
	 * message.append("You were attacked this round. You did not counterattack.\n\n"
	 * ); else message.append(
	 * "You were attacked this round. You attempted to counterattack your attacker(s).\n\n"
	 * ); message.append("\n[b]AP Spent R" + round + ":[/b]\n");
	 * message.append((plr.apMove + plr.apTeleportation) +
	 * " AP on Movement. (Includes Teleportation)\n");
	 * message.append(plr.apRestHeal + " AP on Resting and Healing.\n");
	 * message.append(plr.apSearch + " AP on Searching for Items.\n");
	 * message.append(plr.apStealPeek + " AP on Peeking and Stealing.\n");
	 * message.append(plr.apAttack + " AP on Attacking " + plural(attacksMade,
	 * "player", "players") + ".\n"); message.append(plr.apDefense +
	 * " AP on Defense.\n"); message.append("Total: " + (plr.apMove +
	 * plr.apTeleportation + plr.apSearch + plr.apRestHeal + plr.apStealPeek +
	 * plr.apAttack + plr.apDefense) + "\n\n\n");
	 * message.append("[b]Current Items:[/b]\n"); if (plr.bluePotion > 0)
	 * message.append(plr.bluePotion + "x Blue Potion\n"); if (plr.redPotion >
	 * 0) message.append(plr.redPotion + "x Red Potion\n"); if (plr.skeletonKey
	 * > 0) message.append(plr.skeletonKey + "x Skeleton Key\n"); if
	 * (plr.teleportationScroll > 0) message.append(plr.teleportationScroll +
	 * "x Teleportation Scroll\n"); if (plr.ironSword > 0)
	 * message.append(plr.ironSword + "x Iron Sword\n"); if (plr.woodenShield >
	 * 0) message.append(plr.woodenShield + "x Wooden Shield\n"); if
	 * (plr.throwingKnives > 0) message.append(plr.throwingKnives +
	 * "x Throwing Knives\n"); if (plr.ringOfClarity > 0)
	 * message.append(plr.ringOfClarity + "x Ring of Clarity\n"); if
	 * (plr.ringOfRegeneration > 0) message.append(plr.ringOfRegeneration +
	 * "x Ring of Regeneration\n"); if (plr.ringOfIllusions > 0)
	 * message.append(plr.ringOfIllusions + "x Ring of Illusions\n"); if
	 * (plr.sentinelsCharm > 0) message.append(plr.sentinelsCharm +
	 * "x Sentinel's Charm\n"); if (plr.shadowCharm > 0)
	 * message.append(plr.shadowCharm + "x Shadow Charm\n"); if
	 * (plr.scrollOfMoonlight > 0) message.append(plr.scrollOfMoonlight +
	 * "x Scroll of Moonlight\n"); if (plr.equilibrium > 0)
	 * message.append(plr.equilibrium + "x Equilibrium\n"); if (plr.royalPass >
	 * 0) message.append(plr.royalPass + "x Royal Pass\n"); if (plr.isSpy()) {
	 * message.append("[/spoiler]\n\n\n"); continue; } message.append("\n\n");
	 * if (plr.location.equalsIgnoreCase("Watch Tower"))
	 * revealAllLocations(message, locations); else {
	 * message.append("The following players are in the " + plr.location + ": "
	 * + plr.name); for (Player player : locations.get(plr.location)) { if
	 * (player.invisible || !player.isAlive()) continue; if
	 * (player.name.equals(plr.name)) continue; message.append(", " +
	 * player.name); } } sendPm(new String[] { plr.name }, subject + " - " +
	 * plr.name, message.toString()); } String[] to = new String[spies.size()];
	 * for (int i = 0; i < to.length; ++i) { to[i] = spies.get(i).name; }
	 * boolean watchTower = false; if (locations.get("Watch Tower") != null) {
	 * for (Player player : locations.get("Watch Tower")) { if (player.isSpy())
	 * { watchTower = true; revealAllLocations(spyPm, locations); break; } } }
	 * if (!watchTower) { spyPm.append(
	 * "[b]The Spies in the city have discovered the following players:[/b]\n");
	 * for (LinkedList<Player> location : locations.values()) { String data =
	 * ""; boolean spyPresent = false; for (Player player : location) { data +=
	 * player.name + " is in the " + player.location + ".\n"; if
	 * (player.isSpy()) spyPresent = true; } if (spyPresent) spyPm.append(data);
	 * } } sendPm(to, subject + " - Wolf Team", spyPm.toString()); } private
	 * static void sendPm(String[] to, String subject, String message) { String
	 * destination = Arrays.toString(to); if (consoleSend) {
	 * System.out.println("\n\n\nSend To: " + destination);
	 * System.out.println("Subject: " + subject + "\n\n");
	 * System.out.println(message); return; } message = messageHeader + message;
	 * if (testSend) { message = "Send To: " + destination + "\n\n" + message;
	 * to = new String[] { }; } try { context.makePm(to, new String[] {
	 * "Rainboy" }, subject, message.toString()); } catch (IOException e) {
	 * System.err.println("Unable to send message:"); e.printStackTrace(); }
	 * System.out.println("Message to " + destination + " sent!"); } private
	 * static void revealAllLocations(StringBuilder message, HashMap<String,
	 * LinkedList<Player>> map) { message.append(
	 * "[b]From the Watch Tower, you see the location of all living players:[/b]\n"
	 * ); for (LinkedList<Player> location : map.values()) { for (Player player
	 * : location) { if (player.invisible) message.append(player.name +
	 * " is in the *ESCAPED*.\n"); else if (player.isAlive())
	 * message.append(player.name + " is in the " + player.location + ".\n"); }
	 * } } private static void randomizeLocations(HashMap<String,
	 * LinkedList<Player>> map) { for (LinkedList<Player> location :
	 * map.values()) Collections.shuffle(location); } private static Player
	 * getPlayer(List<Player> players, String name) { for (Player player :
	 * players) { if (player.name.equalsIgnoreCase(name)) return player; }
	 * return null; } private static String plural(int number, String singular,
	 * String plural) { if (Math.abs(number) == 1) return number + " " +
	 * singular; return number + " " + plural; }
	 */
}
