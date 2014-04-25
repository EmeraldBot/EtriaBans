package com.etriacraft.EtriaBans.Commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;
import com.etriacraft.EtriaBans.UUIDFetcher;
import com.etriacraft.EtriaBans.Objects.Mute;

public class MuteCommands {

	EtriaBans plugin;

	public MuteCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}

	private void init() {
		PluginCommand mute = plugin.getCommand("mute");
		PluginCommand tempmute = plugin.getCommand("tempmute");
		PluginCommand unmute = plugin.getCommand("unmute");
		PluginCommand editmute = plugin.getCommand("editmute");
		CommandExecutor exe;
		
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.editmute")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length != 2) {
					s.sendMessage("§3Proper Usage: §6/editmute <player> <timeDiff>");
					s.sendMessage("§3timeDiff = timeUnit");
					s.sendMessage("§3s = sec, m = minute");
					s.sendMessage("§3h = hour, d = day");
					s.sendMessage("§cExample: 4d = 4 Days.");
					return true;
				}
				
				String player = args[0];
				
				UUID uuid = null;
				try {
					uuid = UUIDFetcher.getUUIDOf(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (!Methods.isMuted(uuid)) {
					s.sendMessage("§cYou can't edit the mute of a player who isn't muted.");
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
				
				Methods.editMute(uuid, timeInSeconds);
				s.sendMessage("§7" + player + "'s §amute has been edited.");
				return true;
			}
		}; editmute.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.unmute")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length != 1) {
					s.sendMessage("§3Proper Usage: §6/unmute <player>");
					return true;
				}

				String player = args[0];
				UUID uuid = null;
				try {
					uuid = UUIDFetcher.getUUIDOf(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!Methods.isMuted(uuid)) {
					s.sendMessage("§cYou can't unmute someone who isn't muted.");
					return true;
				}
				
				if (plugin.getConfig().getBoolean("Settings.CanOnlyUnmuteOwnMutes") && !s.hasPermission("etriabans.unmute.override")) {
					Mute mute = Methods.getMute(uuid);
					
					String mutedby = mute.getMutedBy();
					if (!s.getName().equalsIgnoreCase(mutedby)) {
						s.sendMessage("§cYou cannot unmute someone you did not mute.");
						return true;
					}
				}

				Methods.unmutePlayer(uuid, s.getName().toLowerCase());
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player + " §ahas been unmuted.");
					}
				}
				return true;
			}
		}; unmute.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.tempmute")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length < 3) {
					s.sendMessage("§3Proper Usage: §6/tempmute <player> <timeDiff> <reason>");
					s.sendMessage("§3timeDiff = timeUnit");
					s.sendMessage("§3s = sec, m = minute");
					s.sendMessage("§3h = hour, d = day");
					s.sendMessage("§cExample: 4d = 4 Days.");
					return true;
				}

				String player = args[0];
				String timeDiff = args[1];
				String reason = Methods.buildString(args, 2, " ");

				UUID uuid = null;
				try {
					uuid = UUIDFetcher.getUUIDOf(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Player player3 = Bukkit.getPlayer(player);
				if (player3 != null) {
					if (player3 == s) {
						s.sendMessage("§cYou can't mute yourself.");
						return true;
					}
					if (player3.hasPermission("etriabans.exempt.mutes") && !s.hasPermission("etriabans.exempt.mutes.override")) {
						s.sendMessage("§cThat player cannot be muted.");
						return true;
					}
					if (Methods.isMuted(uuid)) {
						s.sendMessage("§cThat player is already muted.");
						return true;
					}
					player = player3.getName();
				}

				if (Methods.isMuted(uuid)) {
					s.sendMessage("§cThat player is already muted.");
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

				Methods.tempMutePlayer(player, reason, s.getName().toLowerCase(), timeInSeconds);
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						if (timeDiff.contains("s")) {
							player2.sendMessage("§7" + player + "§a has been muted for §7" + timeDiff.replace("s", "") + " seconds §afor §7" + reason + "§a.");
						}
						if (timeDiff.contains("m")) {
							player2.sendMessage("§7" + player + "§a has been muted for §7" + timeDiff.replace("m", "") + " minutes §afor §7" + reason + "§a.");
						}
						if (timeDiff.contains("h")) {
							player2.sendMessage("§7" + player + "§a has been muted for §7" + timeDiff.replace("h", "") + " hours §afor §7" + reason + "§a.");
						}
						if (timeDiff.contains("d")) {
							player2.sendMessage("§7" + player + "§a has been muted for §7" + timeDiff.replace("d", "") + " days §afor §7" + reason + "§a.");
						}
					}
				}
				return true;
			}
		}; tempmute.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.mute")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				if (args.length < 2) {
					s.sendMessage("§3Proper Usage: §6/mute <player> <reason>");
					return true;
				}

				String reason = Methods.buildString(args, 1, " ");
				String player = args[0];

				Player player3 = Bukkit.getPlayer(player);
				UUID uuid = null;
				try {
					uuid = UUIDFetcher.getUUIDOf(player3.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (player3 != null) {
					if (player3 == s) {
						s.sendMessage("§cYou can't mute yourself.");
						return true;
					}
					if (player3.hasPermission("etriabans.exempt.mutes") && !s.hasPermission("etriabans.exempt.mutes.override")) {
						s.sendMessage("§cThat player cannot be muted.");
						return true;
					}
					if (Methods.isMuted(uuid)) {
						s.sendMessage("§cThat player is already muted.");
						return true;
					}
					player = player3.getName();
				}
				
				UUID uuid2 = null;
				try {
					uuid2 = UUIDFetcher.getUUIDOf(player);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (Methods.isMuted(uuid2)) {
					s.sendMessage("§cThat player is already muted.");
					return true;
				}

				Methods.mutePlayer(uuid, reason, s.getName().toLowerCase());
				for (Player player2: Bukkit.getOnlinePlayers()) {
					if (player2.hasPermission("etriabans.announce")) {
						player2.sendMessage("§7" + player + "§a has been muted for 7" + reason);
					}
				}

				return true;
			}
		}; mute.setExecutor(exe);
	}
}
