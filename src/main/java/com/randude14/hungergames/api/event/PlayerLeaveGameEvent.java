package com.randude14.hungergames.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.randude14.hungergames.games.HungerGame;

// called when a player leaves a game
public class PlayerLeaveGameEvent extends GameEvent {
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	
	public PlayerLeaveGameEvent(final HungerGame game, final Player player) {
		super(game);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
