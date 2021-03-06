package com.randude14.hungergames.commands;

import com.randude14.hungergames.Config;
import com.randude14.hungergames.Defaults.CommandUsage;
import com.randude14.hungergames.api.event.*;
import com.randude14.hungergames.games.HungerGame;
import com.randude14.hungergames.Defaults.Perm;
import com.randude14.hungergames.GameManager;
import com.randude14.hungergames.Plugin;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.apache.commons.lang.ArrayUtils;


public class CommandHandler implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("In-game use only.");
			return true;
		}
		if (cmd.getLabel().equalsIgnoreCase(Plugin.CMD_USER)) {
			handleUserCommand((Player) sender, cmd, args);
		} else if (cmd.getLabel().equalsIgnoreCase(Plugin.CMD_ADMIN)) {
			handleAdminCommand((Player) sender, cmd, args);
		}
		return false;
	}

	private void handleUserCommand(Player player, Command cmd, String[] args) {
		HungerGame game = null;
		if (args.length == 0) {
			if (!Plugin.checkPermission(player, Perm.USER_HELP)) return;
			
			getUserCommands(player, cmd);
			return;
		}

		else if ("list".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_LIST)) return;

			Plugin.send(player, ChatColor.GREEN, Plugin.getHeadLiner());
			Collection<HungerGame> games = GameManager.getGames();
			if (games.isEmpty()) {
				Plugin.error(player, "No games have been created yet.");
				return;
			}

			for (HungerGame g : games) {
				Plugin.send(player, ChatColor.GOLD, "- " + g.getInfo());
			}
		}

		else if ("join".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_JOIN)) return;

			String name = (args.length == 1) ? Config.getDefaultGame()
					: args[1];
			if (name == null) {
				Plugin.helpCommand(player, CommandUsage.USER_JOIN.getUsage(),
						cmd.getLabel());
				return;
			}

			game = GameManager.getGame(name);
			if (game == null) {
				Plugin.sendDoesNotExist(player, name);
				return;
			}
			
			HungerGame currentSession = GameManager.getSession(player);
			if (currentSession != null) {
				Plugin.error(player,
						"You are already in the game '%s'. Leave that game before joining another.",
						currentSession.getName());
				return;
			}
			if (game.join(player)) {
				String mess = Config.getJoinMessage(game.getSetup());
				mess = mess.replace("<player>", player.getName()).replace(
						"<game>", game.getName());
				Plugin.broadcast(mess);
			}
		}

		else if ("leave".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_LEAVE)) return;

			game = GameManager.getSession(player);
			if (game == null) {
				Plugin.error(player, "You are currently not in a game.");
				return;
			}

			if (game.leave(player)) {
				String mess = Config.getLeaveMessage(game.getSetup());
				mess = mess.replace("<player>", player.getName()).replace(
						"<game>", game.getName());
				Plugin.broadcast(mess);
			}

		}
		
		else if ("quit".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_QUIT)) return;

			game = GameManager.getSession(player);
			if (game == null) {
				Plugin.error(player, "You are currently not in a game.");
				return;
			}

			if (game.quit(player)) {
				String mess = Config.getQuitMessage(game.getSetup());
				mess = mess.replace("<player>", player.getName()).replace(
						"<game>", game.getName());
				Plugin.broadcast(mess);
			}

		}
		
		else if ("rejoin".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_REJOIN)) return;

			game = GameManager.getSession(player);
			if (game == null) {
				Plugin.error(player, "You are currently not in a game.");
				return;
			}
			
			if (game.rejoin(player)) {
				String mess = Config.getRejoinMessage(game.getSetup());
				mess = mess.replace("<player>", player.getName()).replace(
						"<game>", game.getName());
				Plugin.broadcast(mess);
			}

			else {
				Plugin.error(player, "Failed to rejoin %s.", game.getName());
			}

		}

		else if ("sponsor".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_SPONSOR)) return;

			if (args.length < 2) {
				Plugin.send(player, CommandUsage.USER_SPONSOR.getUsage(),
						cmd.getLabel());
				return;
			}

			Player p = Bukkit.getServer().getPlayer(args[1]);
			if (p == null) {
				Plugin.error(player, "%s is not online.", args[1]);
				return;
			}
			if (GameManager.getSession(p) == null || !GameManager.getSession(p).getPlayerStat(p).isPlaying()) {
				Plugin.error(player, "%s is not playing in a game.", p.getName());
				return;
			}
			Plugin.addSponsor(player, p.getName());
		}

		else if ("vote".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_VOTE)) return;

			game = GameManager.getSession(player);
			if (game == null) {
				Plugin.error(player,
					"You must be in a game to vote. "
					+ "You can a game join by '" + CommandUsage.USER_JOIN.getUsage() + "'",
						Plugin.CMD_USER);
				return;
			}
			game.addReadyPlayer(player);
			Plugin.send(player, "You have voted that you are ready.");
		}

		else if ("stat".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.USER_STAT)) return;

			if (args.length == 1) {
				Plugin.send(player, CommandUsage.USER_STAT.getUsage(),
						cmd.getLabel());
				return;
			}

			game = GameManager.getGame(args[1]);
			if (game == null) {
				Plugin.sendDoesNotExist(player, args[1]);
				return;
			}
			Plugin.send(player, ChatColor.GREEN, Plugin.getHeadLiner());
			game.listStats(player);

		}

		else {
			if (!Plugin.checkPermission(player, Perm.USER_HELP)) return;

			getUserCommands(player, cmd);
		}
		if(game != null) {
		    GameManager.saveGame(game);
		}
		else{
		    GameManager.saveGames();
		}
	}

	private void handleAdminCommand(Player player, Command cmd, String[] args) {
		HungerGame game = null;
		GameManager GameManager = Plugin.getGameManager();

		if (args.length == 0) {
			if (!Plugin.hasPermission(player, Perm.ADMIN_HELP)) return;
			
			getAdminCommands(player, cmd);
			return;
		}

		if ("add".equalsIgnoreCase(args[0])) {
			new AddCommand().execute(player, cmd, (String[]) ArrayUtils.removeElement(args, args[0]));
		}

		else if ("remove".equalsIgnoreCase(args[0])) {
			new RemoveCommand().execute(player, cmd, (String[]) ArrayUtils.removeElement(args, args[0]));
		}

		else if ("set".equalsIgnoreCase(args[0])) {
			new SetCommand().execute(player, cmd, (String[]) ArrayUtils.removeElement(args, args[0]));
		}
		
		else if("pause".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.ADMIN_PAUSE)) return;
			
			if (args.length == 1) {
				Plugin.helpCommand(player, CommandUsage.ADMIN_PAUSE.getUsage(), cmd.getLabel());
				return;
			}
			
			game = GameManager.getGame(args[1]);
			if (game == null) {
			    Plugin.error(player, "%s does not exist.", args[1]);
			    return;
			}
			
			if(game.pauseGame(player)) {
				Plugin.broadcast("%s has been paused.", game.getName());
			}
			
		}
		
		else if("resume".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.ADMIN_RESUME)) return;
			
			if (args.length == 1) {
				Plugin.helpCommand(player, CommandUsage.ADMIN_RESUME.getUsage(), cmd.getLabel());
				return;
			}
			
			game = GameManager.getGame(args[1]);
			if (game == null) {
			    Plugin.error(player, "%s does not exist.", args[1]);
			    return;
			}
			
			if(args.length == 2) {
				if(!game.resume(player)) {
					Plugin.error(player, "Failed to resume %s.", game.getName());
				}
				
			}
			
			else {
				int seconds;
				try {
					seconds = Integer.parseInt(args[2]);
				} catch (Exception ex) {
					Plugin.error(player, "'%s' is not an integer.", args[2]);
					return;
				}
				if(!game.resume(player, seconds)) {
					Plugin.error(player, "Failed to resume %s.", game.getName());
				}
				
			}
			
		}

		else if ("kick".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.ADMIN_KICK)) return;

			if (args.length == 1) {
				Plugin.helpCommand(player, CommandUsage.ADMIN_KICK.getUsage(), cmd.getLabel());
				return;
			}

			Player kick = Bukkit.getServer().getPlayer(args[1]);
			if (kick == null) {
			    Plugin.error(player, "%s is not online.", args[1]);
			    return;
			}
			game = GameManager.getSession(kick);
			if (game == null) {
			    Plugin.error(player, "%s is currently not in a game.", kick.getName());
			    return;
			}
			Plugin.broadcast("%s has been kicked from the game %s.", player.getName(), game.getName());
			Plugin.callEvent(new PlayerKickGameEvent(game, kick));
			game.leave(kick);
		}

		else if ("start".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.ADMIN_START)) return;

			if (args.length == 1) {
				Plugin.helpCommand(player, CommandUsage.ADMIN_START.getUsage(),
						cmd.getLabel());
				return;
			}

			game = GameManager.getGame(args[1]);
			if (game == null) {
				Plugin.sendDoesNotExist(player, args[1]);
				return;
			}

			int seconds;

			if (args.length == 3) {
				try {
					seconds = Integer.parseInt(args[2]);
				} catch (Exception ex) {
					Plugin.error(player, "'%s' is not an integer.", args[2]);
					return;
				}
			}

			else {
				seconds = Config.getDefaultTime(game.getSetup());
			}
			if (!game.start(player, seconds)) {
				Plugin.error(player, "Failed to start %s.", game.getName());
			}
		}

		else if ("reload".equalsIgnoreCase(args[0])) {
			if (!Plugin.checkPermission(player, Perm.ADMIN_RELOAD)) return;
			
			Plugin.reload();
			Plugin.send(player, Plugin.getPrefix() + "Reloaded v%s", Plugin
					.getInstance().getDescription().getVersion());
		} else {
			if (!Plugin.checkPermission(player, Perm.ADMIN_HELP)) return;
			
			getAdminCommands(player, cmd);
		}
		if(game != null) {
		    GameManager.saveGame(game);
		}
		else{
		    GameManager.saveGames();
		}
		
	}

	private void getUserCommands(Player player, Command cmd) {
		Plugin.send(player, ChatColor.GREEN, Plugin.getHeadLiner());
		Plugin.helpCommand(player, CommandUsage.USER_LIST.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_JOIN.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_LEAVE.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_QUIT.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_REJOIN.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_SPONSOR.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_VOTE.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.USER_STAT.getUsageAndInfo(), cmd.getLabel());
	}

	private void getAdminCommands(Player player, Command cmd) {
		Plugin.send(player, ChatColor.GREEN, Plugin.getHeadLiner());
		Plugin.helpCommand(player, CommandUsage.ADMIN_ADD_HELP.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.ADMIN_REMOVE_HELP.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.ADMIN_SET_HELP.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.ADMIN_KICK.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.ADMIN_RELOAD.getUsageAndInfo(), cmd.getLabel());
		Plugin.helpCommand(player, CommandUsage.ADMIN_START.getUsageAndInfo(), cmd.getLabel());
	}

}
