package com.randude14.hungergames.commands;

import com.randude14.hungergames.Defaults.Perm;
import com.randude14.hungergames.Defaults.CommandUsage;
import com.randude14.hungergames.GameManager;
import com.randude14.hungergames.Plugin;
import com.randude14.hungergames.api.event.GameCreateEvent;
import com.randude14.hungergames.games.HungerGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 *
 */
public class AddCommand extends SubCommand{

    @Override
    public boolean execute(CommandSender cs, Command cmd, String[] args) {
	Player player = (Player) cs;
	
	if (args.length == 0 || "?".equalsIgnoreCase(args[0])) {
	    Plugin.send(player, ChatColor.GREEN, Plugin.getHeadLiner());
	    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_SPAWNPOINT.getUsageAndInfo(),
		    Plugin.CMD_ADMIN);
	    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_CHEST.getUsageAndInfo(),
		    Plugin.CMD_ADMIN);
	    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_GAME.getUsageAndInfo(),
		    Plugin.CMD_ADMIN);
	    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_ITEMSET.getUsageAndInfo(),
		    Plugin.CMD_ADMIN);
	    return true;
	}

	HungerGame game = GameManager.getGame(args[1]);
	if ("spawnpoint".equalsIgnoreCase(args[0])) {
	    if(!Plugin.checkPermission(player, Perm.ADMIN_ADD_SPAWNPOINT)) return true;
	    
	    if (args.length == 1) {
		    Plugin.send(player, CommandUsage.ADMIN_ADD_SPAWNPOINT.getUsage(), Plugin.CMD_ADMIN);
		    return true;
	    }

	    if (game == null) {
		 Plugin.sendDoesNotExist(player, args[1]);
		 return true;
	    }
	    
	    Plugin.addSpawnAdder(player, game.getName());
	    Plugin.send(player, ChatColor.GREEN,
		    "Left-click blocks to add them as spawn points for %s. Right-click to finish.", game.getName());
	}

	else if ("chest".equalsIgnoreCase(args[0])) {
	    if(!Plugin.checkPermission(player, Perm.ADMIN_ADD_CHEST)) return true;
	    
	    if (args.length == 1) {
		    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_CHEST.getUsage(),
			    Plugin.CMD_ADMIN);
		    return true;
	    }
	    
	    if (game == null) {
		Plugin.sendDoesNotExist(player, args[1]);
		return true;
	    }
	    
	    Plugin.addChestAdder(player, args[1]);
	    Plugin.send(player, ChatColor.GREEN,
		    "Hit a chest to add it to %s.", game.getName());
	}

	else if ("game".equalsIgnoreCase(args[0])) {
	    if(!Plugin.checkPermission(player, Perm.ADMIN_ADD_GAME)) return true;

	    if (args.length == 1) {
		    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_GAME.getUsage(),
			    Plugin.CMD_ADMIN);
	    }

	    if (game != null) {
		    Plugin.error(player, "%s already exists.", args[1]);
		    return true;
	    }
	    if(args.length == 2){
		    GameManager.createGame(args[1]);
	    }
	    else{
		    GameManager.createGame(args[1], args[2]);
	    }
	    GameCreateEvent event = new GameCreateEvent(GameManager.getGame(args[1]));
	    if(event.isCancelled()) {
	    	GameManager.removeGame(args[1]);
	    	Plugin.error(player, "Creation of game %s was cancelled.", args[1]);
	    }
	    else {
	    	Plugin.send(player, ChatColor.GREEN, "%s has been created. To add spawn points, simply", args[1]);
	    	Plugin.send(player, ChatColor.GREEN, "type the command '/%s add spawnpoint <game name>'", Plugin.CMD_ADMIN);
	    }
	    
	}

	else if("itemset".equalsIgnoreCase(args[0])){
	    if(!Plugin.checkPermission(player, Perm.ADMIN_ADD_ITEMSET)) return true;
	    
	    if(args.length == 2){
		    Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_ITEMSET.getUsage(),
			    Plugin.CMD_ADMIN);
	    }

	    if (game == null) {
		    Plugin.sendDoesNotExist(player, args[1]);
		    return true;
	    }
	    game.addItemSet(args[2]);
	}

	else {
		Plugin.error(player, "'%s' is not recognized.", args[0]);
	}
	return true;
    }
}
