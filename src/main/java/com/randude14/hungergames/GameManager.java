package com.randude14.hungergames;

import com.randude14.hungergames.games.HungerGame;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;




public class GameManager implements Listener {
	private static final Plugin plugin = Plugin.getInstance();
	private static Set<HungerGame> games = new TreeSet<HungerGame>();
	private static CustomYaml yaml = new CustomYaml(new File(plugin.getDataFolder(), "games.yml"));
	private static Map<Player, Location> respawnLocation = new HashMap<Player, Location>();

	public static boolean createGame(String name) {
	    HungerGame game = new HungerGame(name);
	    boolean attempt = games.add(game);
	    if(attempt){
		saveGames();
	    }
	    return attempt;
	}

	public static boolean createGame(String name, String setup){
	    HungerGame game = new HungerGame(name, setup);
	    boolean attempt = games.add(game);
	    if(attempt){
		saveGames();
	    }
	    return attempt;
	}
	
	public static boolean removeGame(String name) {
	    HungerGame game = getGame(name);
	    if(game == null) return false;
	    boolean attempt = games.remove(game);
	    if(attempt){
		saveGames();
	    }
	    return attempt;
	}

	public static Set<HungerGame> getGames() {
		return games;
	}

	public static HungerGame getGame(String name) {
		for (HungerGame game : games) {
			if (game.getName().equals(name)) {
				return game;
			}

		}
		return null;
	}

	/**
	 * This does not care about whether the player is actually playing the game or not.
	 * If the player has the potential to rejoin, and therefore has lives, that is the game.
	 * 
	 * @param player
	 * @return the game a player is in
	 */
	public static HungerGame getSession(Player player) {
		for (HungerGame game : games) {
			if (game.contains(player)) {
				return game;
			}

		}
		return null;
	}

	public static boolean doesNameExist(String name) {
		return getGame(name) != null;
	}
	
	public static void addPlayerRespawn(Player player, Location respawn) {
		if(respawn == null) return;
		respawnLocation.put(player, respawn);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void playerKilled(PlayerDeathEvent event) {
		Player killed = event.getEntity();
		HungerGame gameOfKilled = getSession(killed);
		if (gameOfKilled == null) return;
		
		Player killer = killed.getKiller();
		if (killer != null) {
			HungerGame gameOfKiller = getSession(killer);

			if (gameOfKilled.equals(gameOfKiller)) {
				gameOfKiller.killed(killer, killed);
			}
		}
		else {
			gameOfKilled.killed(killed);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void playerRespawn(PlayerRespawnEvent event) {
	    if(respawnLocation.containsKey(event.getPlayer())){
		event.setRespawnLocation(respawnLocation.get(event.getPlayer()));
		respawnLocation.remove(event.getPlayer());
	    }
	    
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public static void playerQuit(PlayerQuitEvent event) {
		playerLeftServer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public static void playerKick(PlayerKickEvent event) {
		playerLeftServer(event.getPlayer());
	}

	private static void playerLeftServer(Player player) {
		HungerGame game = getSession(player);
		if (game == null) return;
		game.quit(player);
		String mess = Config.getQuitMessage(game.getSetup())
			.replace("<player>", player.getName())
			.replace("<game>", game.getName());
		Plugin.broadcast(mess);
	}

	public static void loadGames() {
		FileConfiguration config = yaml.getConfig();
		ConfigurationSection gamesSection = config.getConfigurationSection("games");
		if (gamesSection == null) return;
		games.clear();
		for (String name : gamesSection.getKeys(false)) {
			ConfigurationSection gameSection = gamesSection
					.getConfigurationSection(name);
			HungerGame game = new HungerGame(name);
			game.loadFrom(gameSection);
			games.add(game);
		}
		
	}

	public static void saveGames() {
		FileConfiguration config = yaml.getConfig();
		ConfigurationSection section = config.createSection("games");
		for (HungerGame game : games) {
			ConfigurationSection saveSection = section.createSection(game.getName());
			game.saveTo(saveSection);
		}
		yaml.save();
	}
	
	public static void reloadGame(HungerGame game){
		FileConfiguration config = yaml.getConfig();
		ConfigurationSection gameSection = config.getConfigurationSection("games." + game.getName());
		if (gameSection == null) {
			return;
		}
		game.loadFrom(gameSection);
		games.add(game);
	}

	public static void saveGame(HungerGame game){
		FileConfiguration config = yaml.getConfig();
		ConfigurationSection section = config.getConfigurationSection("games");
		if(section == null){
		    section = config.createSection("games");
		}
		ConfigurationSection saveSection = section.createSection(game.getName());
		game.saveTo(saveSection);
		yaml.save();
	}
}
