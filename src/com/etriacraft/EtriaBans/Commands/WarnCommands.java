package com.etriacraft.EtriaBans.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;

public class WarnCommands {

EtriaBans plugin;
	
	public WarnCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}
	
	private void init() {
		PluginCommand warn = plugin.getCommand("warn");
		CommandExecutor exe;
		
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.warn")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length < 2) {
					s.sendMessage("§3Proper Usage: §6/warn <player> <reason>");
					return true;
				}
				
				Player player = Bukkit.getPlayer(args[0]);
				String reason = Methods.buildString(args, 1, " ");
				
				if (player == null) {
					s.sendMessage("§cThat player is not online.");
					return true;
				}
				
				if (player.hasPermission("etriabans.exempt.warns") && !s.hasPermission("etriabans.exempt.warns.override")) {
					s.sendMessage("§cYou cannot warn that player.");
					return true;
				}
				
				if (player == s) {
					s.sendMessage("§cYou cannot warn yourself.");
					return true;
				}
				
				Methods.warnPlayer(player.getName().toLowerCase(), s.getName().toLowerCase(), reason);
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player.getName() + " §ahas been warned by §7" + s.getName() + " §afor §7" + reason);
					}
				}
				return true;
			}
		}; warn.setExecutor(exe);
		
	}
}
