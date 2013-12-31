package com.etriacraft.EtriaBans.Commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;

public class BanCommands {

	EtriaBans plugin;

	public BanCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}

	private void init() {
		PluginCommand ban = plugin.getCommand("ban");
		PluginCommand tempban = plugin.getCommand("tempban");
		PluginCommand unban = plugin.getCommand("unban");
		PluginCommand editban = plugin.getCommand("editban");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.editban")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length != 2) {
					s.sendMessage("§3Proper Usage: §6/editban <player> <timeDiff>");
					s.sendMessage("§3timeDiff = timeUnit");
					s.sendMessage("§3s = sec, m = minute");
					s.sendMessage("§3h = hour, d = day");
					s.sendMessage("§cExample: 4d = 4 Days.");
					return true;
				}
				
				String player = args[0];
				
				if (!Methods.isBanned(player)) {
					s.sendMessage("§cYou can't edit the ban of a player who isn't banned.");
					return true;
				}
				
				String timeDiff = args[1];
				
				int timeInSeconds = 0;
				if (timeDiff.contains("s")) { // banning in seconds.
					timeInSeconds = Integer.parseInt(timeDiff.replace("s", "")); 
				}
				if (timeDiff.contains("m")) { // Banning in mutes.
					timeInSeconds = Integer.parseInt(timeDiff.replace("m", "")) * 60; // minute * 60 = seconds
				}
				if (timeDiff.contains("h")) { // Banning in Hours
					timeInSeconds = Integer.parseInt(timeDiff.replace("h", "")) * 3600;
				}
				if (timeDiff.contains("d")) {
					timeInSeconds = Integer.parseInt(timeDiff.replace("d", "")) * 86400;
				}
				if (timeInSeconds == 0) {
					s.sendMessage("§cImproper Time Format");
					return true;
				}
				
				Methods.editBan(player, timeInSeconds);
				
				return true;
			}
		}; editban.setExecutor(exe);
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.unban")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length != 1) {
					s.sendMessage("§3Proper Usage: §6/unban <player>");
					return true;
				}

				String player = args[0];
				if (!Methods.isBanned(player)) {
					s.sendMessage("§cYou can't unban someone who isn't banned.");
					return true;
				}

				if (plugin.getConfig().getBoolean("Settings.CanOnlyUnbanOwnBans") && !s.hasPermission("etriabans.unban.override")) {
					ResultSet ban = Methods.getCurrentBan(player);
					try {
						String banner = ban.getString("bannedby");
						
						if (!s.getName().equalsIgnoreCase(banner)) {
							s.sendMessage("§cYou can only unban a player you banned.");
							return true;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				Methods.unbanPlayer(player, s.getName());
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player + " §ahas been unbanned.");
					}
				}
				return true;
			}
		}; unban.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.tempban")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}

				if (args.length < 3) {
					s.sendMessage("§3Proper Usage: §6/tempban <player> <timeDiff> <reason>");
					s.sendMessage("§3timeDiff = timeUnit");
					s.sendMessage("§3s = sec, m = minute");
					s.sendMessage("§3h = hour, d = day");
					s.sendMessage("§cExample: 4d = 4 Days.");
					return true;
				}

				String player = args[0];
				String timeDiff = args[1];
				String reason = Methods.buildString(args, 2, " ");

				Player player3 = Bukkit.getPlayer(player);
				if (player3 != null) {
					if (player3 == s) {
						s.sendMessage("§cYou can't ban yourself.");
						return true;
					}
					if (Methods.isBanned(player3.getName())) {
						s.sendMessage("§cThat player is already banned.");
						return true;
					}
					
					if (player3.hasPermission("etriabans.exempt.bans") && !s.hasPermission("etriabans.exempt.bans.override")) {
						s.sendMessage("§cThat player cannot be banned.");
						return true;
					}
				}

				if (Methods.isBanned(player)) {
					s.sendMessage("§cThat player is already banned.");
					return true;
				}
				
				int timeInSeconds = 0;
				if (timeDiff.contains("s")) { // banning in seconds.
					timeInSeconds = Integer.parseInt(timeDiff.replace("s", "")); 
				}
				if (timeDiff.contains("m")) { // Banning in mutes.
					timeInSeconds = Integer.parseInt(timeDiff.replace("m", "")) * 60; // minute * 60 = seconds
				}
				if (timeDiff.contains("h")) { // Banning in Hours
					timeInSeconds = Integer.parseInt(timeDiff.replace("h", "")) * 3600;
				}
				if (timeDiff.contains("d")) {
					timeInSeconds = Integer.parseInt(timeDiff.replace("d", "")) * 86400;
				}
				if (timeInSeconds == 0) {
					s.sendMessage("§cImproper Time Format");
					return true;
				}

				Methods.tempBanPlayer(player, reason, s.getName(), timeInSeconds);
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						if (timeDiff.contains("s")) {
							player2.sendMessage("§7" + player + "§a has been banned for §7" + timeDiff.replace("s", "") + " seconds.");
						}
						if (timeDiff.contains("m")) {
							player2.sendMessage("§7" + player + "§a has been banned for §7" + timeDiff.replace("m", "") + " minutes");
						}
						if (timeDiff.contains("h")) {
							player2.sendMessage("§7" + player + "§a has been banned for §7" + timeDiff.replace("h", "") + " hours.");
						}
						if (timeDiff.contains("d")) {
							player2.sendMessage("§7" + player + "§a has been banned for §7" + timeDiff.replace("d", "") + " days.");
						}
					}
				}
				return true;
			}
		}; tempban.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.ban")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length < 2) {
					s.sendMessage("§3Proper Usage: §6/ban <player> <reason>");
					return true;
				}


				String reason = Methods.buildString(args, 1, " ");
				String player = args[0];
				
				Player player3 = Bukkit.getPlayer(player);
				if (player3 != null) {
					if (player3 == s) {
						s.sendMessage("§cYou can't ban yourself.");
						return true;
					}
					if (player3.hasPermission("etriabans.exempt.bans") && !s.hasPermission("etriabans.exempt.bans.override")) {
						s.sendMessage("§cYou cannot ban that player.");
						return true;
					}
					if (Methods.isBanned(player3.getName())) {
						s.sendMessage("§cThat player is already banned.");
						return true;
					}
				}

				if (Methods.isBanned(player)) {
					s.sendMessage("§cThat player is already banned.");
					return true;
				}

				Methods.banPlayer(player, reason, s.getName());
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player + "§a has been banned for §7" + reason);
					}
				}

				return true;	
			}
		}; ban.setExecutor(exe);
	}

}