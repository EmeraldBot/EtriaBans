package com.etriacraft.EtriaBans;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
		if (Methods.isMuted(p.getUniqueId())) {
			List<String> blockedCommandsDuringMute = plugin.getConfig().getStringList("Settings.BlockedCommandsDuringMute");
			for (String command: blockedCommandsDuringMute) {
				if (e.getMessage().startsWith("/" + command)) {
					e.setCancelled(true);
					p.sendMessage("§cYou cannot perform that command while muted.");
				}
			}
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (Methods.isMuted(player.getUniqueId()) && !player.hasPermission("etriabans.exempt.mutes")) {
			e.setCancelled(true);
			player.sendMessage("§cYou cannot chat while muted.");
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		String ip = Methods.getLoggedIP(player.getName());
		if (ip == null) { // We don't have an IP logged for this player.
			Methods.logNewIP(player.getUniqueId(), player.getName().toLowerCase(), e.getAddress().getHostAddress());
		} else {
			if (!ip.equals(e.getAddress().getHostAddress())) {
				Methods.updateIP(player.getUniqueId(), e.getAddress().getHostAddress());
			}
		}
		if (Methods.isIPBanned(e.getAddress().getHostAddress()) && !player.hasPermission("etriabans.exempt.bans")) {
			e.disallow(Result.KICK_BANNED, "§cThis IP has been banned.");
			return;
		}

		UUID uuid = null;
		try {
			uuid = UUIDFetcher.getUUIDOf(player.getName());
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		if (Methods.isBanned(uuid)) {
			if (!player.hasPermission("etriabans.exempt.bans")) {
				if (!Methods.isBanTemp(uuid)) {
					e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(uuid));
					return;
				} if (Methods.isBanTemp(uuid)) {
					Date unbandate = Methods.getUnbanDate(uuid);
					Date currentDate = Methods.getCurrentDateAsDate();

					long timeUntilUnban = (unbandate.getTime() - currentDate.getTime()) / 1000; // Returns time in seconds.
					if (timeUntilUnban >= 86400) { // They have at least 24 hours left.
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(uuid) + ". §cYour ban expires in §a~" + timeUntilUnban / 86400 + " days.");
						return;
					}
					if (timeUntilUnban >= 3600 && timeUntilUnban < 86400) { // They have at least 1 hour, but less than 24 hours left.)
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(uuid) + ". §cYour ban expires in §a~" + timeUntilUnban / 3600 + " hours.");
						return;
					}
					if (timeUntilUnban >= 60 && timeUntilUnban < 3600) { // They have at least 1 minute left, but no more than 1 hour left.)
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(uuid) + ". §cYour ban expires in §a~" + timeUntilUnban / 60 + " minutes.");
						return;
					}
					if (timeUntilUnban >= 1 && timeUntilUnban < 60) { // They have under 1 minute.
						e.disallow(Result.KICK_BANNED, "§cYou have been banned for: §f" + Methods.getBanReason(uuid) + ". §cYour ban expires in §a~" + timeUntilUnban + " seconds.");
						return;
					}
					if (timeUntilUnban <= 0) { // Ban expired.
						e.allow();
						Methods.unbanPlayer(uuid, "CONSOLE");
						return;
					}
				}
			}
		}
		if (plugin.getConfig().getBoolean("Settings.CheckIPOnLogin")) {
			Set<String> players = Methods.getPlayersWithIP(ip);
			Set<String> players2 = new HashSet<String>();
			for (String player2: players) {
				UUID id2 = null;
				try {
					id2 = UUIDFetcher.getUUIDOf(player2);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (Methods.isBanned(id2)) {
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
