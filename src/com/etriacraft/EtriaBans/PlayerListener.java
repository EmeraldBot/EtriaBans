package com.etriacraft.EtriaBans;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class PlayerListener implements Listener {

	EtriaBans plugin;

	public PlayerListener(EtriaBans plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		List<String> blockedCommandsDuringMute = plugin.getConfig().getStringList("Settings.BlockedCommandsDuringMute");
		for (String command: blockedCommandsDuringMute) {
			if (e.getMessage().startsWith("/" + command)) {
				e.setCancelled(true);
				p.sendMessage("§cYou cannot perform that command while muted.");
			}
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (Methods.isMuted(player.getName().toLowerCase()) && !player.hasPermission("etriabans.exempt.mutes")) {
			e.setCancelled(true);
			player.sendMessage("§cYou cannot chat while muted.");
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		String ip = Methods.getLoggedIP(player.getName());
		if (ip == null) { // We don't have an IP logged for this player.
			Methods.logNewIP(player.getName().toLowerCase(), e.getAddress().getHostAddress());
		} else {
			if (!ip.equals(e.getAddress().getHostAddress())) {
				Methods.updateIP(player.getName().toLowerCase(), e.getAddress().getHostAddress());
			}
		}
		if (Methods.isIPBanned(e.getAddress().getHostAddress()) && !player.hasPermission("etriabans.exempt.bans")) {
			e.disallow(Result.KICK_BANNED, "§cThis IP has been banned.");
			return;
		}

		
		if (Methods.isBanned(player.getName())) {
			if (!player.hasPermission("etriabans.exempt.bans")) {
				if (!Methods.isBanTemp(player.getName())) {
					e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(player.getName()));
					return;
				} if (Methods.isBanTemp(player.getName())) {
					Date unbandate = Methods.getUnbanDate(player.getName());
					Date currentDate = Methods.getCurrentDateAsDate();

					long timeUntilUnban = (unbandate.getTime() - currentDate.getTime()) / 1000; // Returns time in seconds.
					if (timeUntilUnban >= 86400) { // They have at least 24 hours left.
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(player.getName()) + ". §cYour ban expires in §a~" + timeUntilUnban / 86400 + " days.");
						return;
					}
					if (timeUntilUnban >= 3600 && timeUntilUnban < 86400) { // They have at least 1 hour, but less than 24 hours left.)
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(player.getName()) + ". §cYour ban expires in §a~" + timeUntilUnban / 3600 + " hours.");
						return;
					}
					if (timeUntilUnban >= 60 && timeUntilUnban < 3600) { // They have at least 1 minute left, but no more than 1 hour left.)
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(player.getName()) + ". §cYour ban expires in §a~" + timeUntilUnban / 60 + " minutes.");
						return;
					}
					if (timeUntilUnban >= 1 && timeUntilUnban < 60) { // They have under 1 minute.
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(player.getName()) + ". §cYour ban expires in §a~" + timeUntilUnban + " seconds.");
						return;
					}
					if (timeUntilUnban <= 0) { // Ban expired.
						e.allow();
						Methods.unbanPlayer(player.getName(), "CONSOLE");
						return;
					}
				}
			}
		}
		if (plugin.getConfig().getBoolean("Settings.CheckIPOnLogin")) {
			Set<String> players = Methods.getPlayersWithIP(ip);
			Set<String> players2 = new HashSet<String>();
			for (String player2: players) {
				if (Methods.isBanned(player2)) {
					if (Methods.getLoggedIP(player2).equalsIgnoreCase(ip)) {
						players2.add(player2);
					}
				}
			}

			if (!players2.isEmpty()) {
				for (Player player4: Bukkit.getOnlinePlayers()) {
					if (player4.hasPermission("etriabans.announce")) {
						player4.sendMessage("§7" + player.getName() + " §chas the same IP as the following banned users:" );
						player4.sendMessage("§3" + players2.toString());
					}
				}
			}
		}
	}
}
