package com.etriacraft.EtriaBans.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;

public class KickCommands {

	
EtriaBans plugin;
	
	public KickCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}
	
	private void init() {
		PluginCommand kick = plugin.getCommand("kick");
		CommandExecutor exe;
		
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.kick")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length < 2) {
					s.sendMessage("§3Proper Usage: §6/kick <player> <reason>");
					return true;
				}
				
				Player player = Bukkit.getPlayer(args[0]);
				String reason = Methods.buildString(args, 1, " ");

				if (player == null) {
					s.sendMessage("§cThat player is not online.");
					return true;
				}
				
				if (player.hasPermission("etriabans.exempt.kicks") && !s.hasPermission("etriabans.exempt.kicks.override")) {
					s.sendMessage("§cYou cannot kick this person.");
					return true;
				}
				
				if (player == s) {
					s.sendMessage("§cYou cannot kick yourself.");
					return true;
				}
				
				Methods.kickPlayer(player.getName().toLowerCase(), s.getName(), reason);
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player.getName() + "§a has been kicked for §7" + reason + ".");
					}
				}
				return true;
			}
		}; kick.setExecutor(exe);
		
	}
}
