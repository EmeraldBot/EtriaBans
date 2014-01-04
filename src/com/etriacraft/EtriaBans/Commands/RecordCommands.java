package com.etriacraft.EtriaBans.Commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;
import com.etriacraft.EtriaBans.Objects.Ban;
import com.etriacraft.EtriaBans.Objects.Mute;

public class RecordCommands {

	EtriaBans plugin;

	public RecordCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}

	private void init() {
		PluginCommand records = plugin.getCommand("records");
		PluginCommand banrecords = plugin.getCommand("banrecords");
		PluginCommand kickrecords = plugin.getCommand("kickrecords");
		PluginCommand muterecords = plugin.getCommand("muterecords");
		PluginCommand warnrecords = plugin.getCommand("warnrecords");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					s.sendMessage("§3Proper Usage: §6/muterecords [player|id] [#|past|current]");
					return true;
				}

				if (args[0].equalsIgnoreCase("id")) {
					if (!s.hasPermission("etriabans.muterecords.byid")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}

					int id = Integer.parseInt(args[1]);
					ResultSet mute = Methods.getMuteByID(id);

					try {
						if (!mute.next()) {
							s.sendMessage("§cNo mute exists with that ID.");
							return true;
						}

						s.sendMessage("§c-----§aEtriaBans Record§c-----");
						s.sendMessage("§3ID: §a" + id);
						s.sendMessage("§3Player: §a" + mute.getString("player"));
						s.sendMessage("§3Muted On: §a" + mute.getString("mutedate"));
						s.sendMessage("§3Muted By: §a" + mute.getString("mutedby"));
						s.sendMessage("§3Reason: §a" + mute.getString("reason"));
						s.sendMessage("§3Unmute Date: §a" + mute.getString("unmutedate"));
						s.sendMessage("§3Unmuted By: §a" + mute.getString("unmutedby"));

						return true;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				String player = args[0].toLowerCase();
				if (player.equalsIgnoreCase(s.getName())) {
					if (!s.hasPermission("etriabans.muterecords.own") && !s.hasPermission("etriabans.muterecords.all")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
				}

				if (!args[1].equalsIgnoreCase("past") && !args[1].equalsIgnoreCase("current")) {
					s.sendMessage("§3Proper Usage: §6/muterecords [Player] [past|current]");
					return true;	
				}

				if (args[1].equalsIgnoreCase("past")) {
					s.sendMessage("§c-----§a" + player + "'s Mute Record§c-----");
					s.sendMessage("§6ID §f - §3Reason");
					ResultSet mutes = Methods.getAllPreviousMutes(player);

					try {
						while (mutes.next()) {
							int id = mutes.getInt("id");
							String reason = mutes.getString("reason");
							s.sendMessage("§6" + id + " §f- §3" + reason);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (args[1].equalsIgnoreCase("current")) {
					if (!Methods.isMuted(player)) {
						s.sendMessage("§7" + player + " §adoes not have a current mute.");
						return true;
					}

					Mute mute = Methods.getMute(player);
					//					ResultSet mute = Methods.getCurrentMute(player);

					int id = mute.getID();
					String mutedate = mute.getDate();
					int length = (int) mute.getLength();
					String unmutedate = mute.getUnmuteDate();
					String mutedby = mute.getMutedBy();
					String reason = mute.getReason();

					s.sendMessage("§c-----§a" + player + "'s Current Mute§c-----");
					s.sendMessage("§3ID: §a" + id);
					s.sendMessage("§3Player: §a" + player);
					s.sendMessage("§3Muted On: §a" + mutedate);
					s.sendMessage("§3Muted By: §a" + mutedby);
					if (Methods.isMuteTemp(player)) {
						if (length >= 86400) {
							s.sendMessage("§3Mute Length: §a~" + (length / 86400) + " days");
						}
						if (length < 86400 && length >= 3600) {
							s.sendMessage("§3Mute Length: §a~" + (length / 3600) + " hours");
						}
						if (length < 3600 && length >= 60) {
							s.sendMessage("§3Mute Length: §a~" + (length / 60) + " minutes");
						}
						if (length < 60) {
							s.sendMessage("§3Mute Length: §a~" + length + " seconds");
						}
						s.sendMessage("§3Unmute Date: §a" + unmutedate);
					}
					s.sendMessage("§3Reason: §a" + reason);
					return true;
				}
				return true;
			}
		}; muterecords.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length != 2) {
					s.sendMessage("§3Proper Usage: §6/warnrecords [id|player] [#|PlayerName]");
					return true;
				}

				if (args[0].equalsIgnoreCase("id")) {
					if (!s.hasPermission("etriabans.warnrecords.byid")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}

					int id = Integer.parseInt(args[1]);
					ResultSet warn = Methods.getWarnByID(id);

					try {
						if (!warn.next()) {
							s.sendMessage("§cThere is no warning with that ID.");
							return true;
						}

						s.sendMessage("§c-----§aEtriaBans Warn Record§c-----");
						String player = warn.getString("player");
						String date = warn.getString("date");
						String warner = warn.getString("warner");
						String reason = warn.getString("reason");

						s.sendMessage("§3ID: §a" + id);
						s.sendMessage("§3Player: §a" + player);
						s.sendMessage("§3Date: §a" + date);
						s.sendMessage("§3Warned By: §a" + warner);
						s.sendMessage("§3Reason: §a" + reason);

						return true;
					} catch (SQLException e) {
						e.printStackTrace();
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("player")) {
					String player = args[1];

					if (player.equalsIgnoreCase(s.getName())) {
						if (!s.hasPermission("etriabans.warnrecords.own") && !s.hasPermission("etriabans.warnrecords.all")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
					} else {
						if (!s.hasPermission("etriabans.warnrecords.all")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
					}

					ResultSet warns = Methods.getAllPreviousWarns(player);
					try {
						if (!warns.next()) {
							s.sendMessage("§cThere are no prior warnings to display.");
							return true;
						}

						s.sendMessage("§c----§aEtriaBans Warn Record§c-----");
						s.sendMessage("§6ID §f- §3Reason");
						while (warns.next()) {
							s.sendMessage("§6" + warns.getString("id") + " §f- §3" + warns.getString("reason"));
						}
						return true;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					s.sendMessage("§3Proper Usage: §6/warnrecords [id|player] [#|PlayerName]");
					return true;
				}
				return true;
			}
		}; warnrecords.setExecutor(exe);
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length != 2) {
					s.sendMessage("§3Proper Usage: §6/kickrecords [id|player] [#|PlayerName]");
					return true;
				}

				if (args[0].equalsIgnoreCase("id")) {
					if (!s.hasPermission("etriabans.kickrecords.byid")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}

					int id = Integer.parseInt(args[1]);

					ResultSet kick = Methods.getKickByID(id);

					try {
						if (!kick.next()) {
							s.sendMessage("§cNo kick exists with that ID.");
							return true;
						}

						s.sendMessage("§c-----§aEtriaBans Kick Record§c-----");
						String player = kick.getString("player");
						String date = kick.getString("date");
						String kicker = kick.getString("kicker");
						String reason = kick.getString("reason");

						s.sendMessage("§3ID: §a" + id);
						s.sendMessage("§3Player: §a" + player);
						s.sendMessage("§3Date: §a" + date);
						s.sendMessage("§3Kicked By: §a" + kicker);
						s.sendMessage("§3Reason: §a" + reason);
						return true;
					} catch (SQLException e) {
						e.printStackTrace();
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("player")) {
					String player = args[1];

					if (player.equalsIgnoreCase(s.getName())) {
						if (!s.hasPermission("etriabans.kickrecords.own") && !s.hasPermission("etriabans.kickrecords.all")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
					} else {
						if (!s.hasPermission("etriabans.kickrecords.all")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
					}

					ResultSet kicks = Methods.getAllPreviousKicks(player);
					try {
						if (!kicks.next()) {
							s.sendMessage("§cThere are no prior kicks to display.");
							return true;
						}

						s.sendMessage("§c----§aEtriaBans Kick Record§c-----");
						s.sendMessage("§6ID §f- §3Reason");
						while (kicks.next()) {
							s.sendMessage("§6" + kicks.getString("id") + " §f- §3" + kicks.getString("reason"));
						}
						return true;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					s.sendMessage("§3Proper Usage: §6/kickrecords [id|player] [#|PlayerName]");
					return true;
				}
				return true;
			}
		}; kickrecords.setExecutor(exe);
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					s.sendMessage("§3Proper Usage: §6/banrecords [player|id] [#|past|current]");
					return true;
				}

				if (args[0].equalsIgnoreCase("id")) {
					if (!s.hasPermission("etriabans.banrecords.byid")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}

					int id = Integer.parseInt(args[1]);
					ResultSet ban = Methods.getBanByID(id);

					try {
						if (!ban.next()) {
							s.sendMessage("§cNo ban exists with that ID.");
							return true;
						}

						s.sendMessage("§c-----§aEtriaBans Record§c-----");
						s.sendMessage("§3ID: §a" + id);
						s.sendMessage("§3Player: §a" + ban.getString("player"));
						s.sendMessage("§3Ban Date: §a" + ban.getString("bandate"));
						s.sendMessage("§3Reason: §a" + ban.getString("reason"));
						s.sendMessage("§3Banned By: §a" + ban.getString("bannedby"));
						s.sendMessage("§3Unban Date: §a" + ban.getString("unbandate"));
						s.sendMessage("§3Unbanned By: §a" + ban.getString("unbannedby"));

						return true;
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return true;
				} 

				String player = args[0].toLowerCase();
				if (player.equalsIgnoreCase(s.getName())) {
					if (!s.hasPermission("etriabans.banrecords.own") && !s.hasPermission("etriabans.banrecords.all")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
				}

				if (!args[1].equalsIgnoreCase("past") && !args[1].equalsIgnoreCase("current")) {
					s.sendMessage("§3Proper Usage: §6/banrecords [Player] [past|current]");
					return true;
				}

				if (args[1].equalsIgnoreCase("past")) {
					s.sendMessage("§c-----§a" + player + "'s Record§c-----");
					s.sendMessage("§6ID §f- §3Reason");
					ResultSet bans = Methods.getAllPreviousBans(player);
					try {
						while (bans.next()) {
							int id = bans.getInt("id");
							String reason = bans.getString("reason");
							s.sendMessage("§6" + id + " §f- §3" + reason);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (args[1].equalsIgnoreCase("current")) {
					if (!Methods.isBanned(player)) {
						s.sendMessage("§7" + player + " §adoes not have a current ban.");
						return true;
					}

					Ban ban = Methods.getBan(player);
					
					int id = ban.getID();
					String bandate = ban.getDate();
					int banlength = (int) ban.getLength();
					String unbandate = ban.getUnbanDate();
					String bannedby = ban.getBannedBy();
					String reason = ban.getReason();

					s.sendMessage("§c-----§aEtriaBans Record§c-----");
					s.sendMessage("§3ID: §a" + id);
					s.sendMessage("§3Player: §a" + player);
					s.sendMessage("§3Banned On: §a" + bandate);
					if (Methods.isBanTemp(player)) {
						if (banlength >= 86400) {
							s.sendMessage("§3Ban Length: §a~" + (banlength / 86400) + " days");
						}
						if (banlength < 86400 && banlength >= 3600) {
							s.sendMessage("§3Ban Length: §a~" + (banlength / 3600) + " hours");
						}
						if (banlength < 3600 && banlength >= 60) {
							s.sendMessage("§3Ban Length: §a~" + (banlength / 60) + " minutes");
						}
						if (banlength < 60) {
							s.sendMessage("§3Ban Length: §a~" + banlength + " seconds");
						}
						s.sendMessage("§3Unban Date: §a" + unbandate);
					}
					s.sendMessage("§3Banned By: §a" + bannedby);
					s.sendMessage("§3Reason: §a" + reason);
					return true;
				}
				return true;
			}
		}; banrecords.setExecutor(exe);

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					if (!s.hasPermission("etriabans.records.own")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					String player = s.getName().toLowerCase();

					int bans = Methods.getTotalBans(player);
					int mutes = Methods.getTotalMutes(player);
					int warns = Methods.getTotalWarns(player);
					int kicks = Methods.getTotalKicks(player);
					String ip = Methods.getLoggedIP(s.getName().toLowerCase());

					s.sendMessage("§c-----§aEtriaBans Record§c-----");
					if (s.hasPermission("etriabans.records.canseeip")) {
						s.sendMessage("§3IP Address: §a" + ip);
					}
					s.sendMessage("§3Total Bans: §a" + bans);
					if (Methods.isMuted(player)) {
						s.sendMessage("§3Total Mutes: §a" + (mutes + 1));
					} else {
						s.sendMessage("§3Total Mutes: §a" + mutes);
					}
					s.sendMessage("§3Total Warns: §a" + warns);
					s.sendMessage("§3Total Kicks: §a" + kicks);
					if (Methods.isMuted(player)) {
						s.sendMessage("§3Current Mute For: §a" + Methods.getMuteReason(player));
					}
					return true;
				}

				if (args.length == 1) {
					if (!s.hasPermission("etriabans.records.all")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}

					String player = args[0].toLowerCase();

					int bans = Methods.getTotalBans(player);
					int mutes = Methods.getTotalMutes(player);
					int warns = Methods.getTotalWarns(player);
					int kicks = Methods.getTotalKicks(player);
					String ip = Methods.getLoggedIP(player.toLowerCase());

					s.sendMessage("§c-----§aEtriaBans Record§c-----");
					s.sendMessage("§3Player: §a" + player);
					if (s.hasPermission("etriabans.records.canseeip")) {
						if (ip != null) {
							s.sendMessage("§3IP Address: §a" + ip);
						}
					}
					if (Methods.isBanned(player)) {
						s.sendMessage("§3Total Bans: §a" + (bans + 1));
					} else {
						s.sendMessage("§3Total Bans: §a" + bans);
					}
					s.sendMessage("§3Total Mutes: §a" + mutes);
					s.sendMessage("§3Total Warns: §a" + warns);
					s.sendMessage("§3Total Kicks: §a" + kicks);

					if (Methods.isBanned(player)) {
						s.sendMessage("§3Current Ban For: §a" + Methods.getBanReason(player));
					}
					if (Methods.isMuted(player)) {
						s.sendMessage("§3Current Mute For: §a" + Methods.getMuteReason(player));
					}

					return true;
				} else {
					s.sendMessage("§3Proper Usage: §6/records <Player>");
					return true;
				}
			}
		}; records.setExecutor(exe);

	}
}
