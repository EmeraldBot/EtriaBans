package com.etriacraft.EtriaBans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.etriacraft.EtriaBans.Objects.*;

public class Methods {

	EtriaBans plugin;

	public Methods(EtriaBans plugin) {
		this.plugin = plugin;
	}

	private static int importPlayerDataTask, importCurrentBanTask, importBanDataTask, importMuteDataTask, importWarnDataTask, importkickDataTask, importCurrentMuteTask, importIPBanDataTask;

	public static HashMap<String, Boolean> bannedPlayers = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> mutedPlayers = new HashMap<String, Boolean>();
	public static Set<String> bannedIPs = new HashSet<String>();

	public static void loadIPBans() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_ipbans");
		try {
			while (rs.next()) {
				String ip = rs.getString("ip");
				bannedIPs.add(ip);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void loadBans() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_bans");
		try {
			while (rs.next()) {
				String player = rs.getString("player");
				if (!rs.getString("unbandate").equals("0")) {
					bannedPlayers.put(player, true);
				} else {
					bannedPlayers.put(player, false);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getBannedPlayers() {
		return bannedPlayers.keySet();
	}

	public static Set<String> getMutedPlayers() {
		return mutedPlayers.keySet();
	}

	public static Set<String> getBannedIPs() {
		return bannedIPs;
	}

	public static void loadMutes() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_mutes");
		try {
			while (rs.next()) {
				String player = rs.getString("player");
				if (!rs.getString("unmutedate").equals("0")) {
					mutedPlayers.put(player, true);
				} else {
					mutedPlayers.put(player, false);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void banIP(String ip, String reason, String bannedby) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_ipbans (ip, date, reason, bannedby) VALUES ("
				+ "'" + ip + "', "
				+ "'" + reason +  "', "
				+ "'" + bannedby.toLowerCase() + "');");
		bannedIPs.add(ip);
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (player.getAddress().getAddress().getHostAddress().equals(ip) && !player.hasPermission("etriabans.exempt.bans")) {
				player.kickPlayer("§cThis IP has been banned for: §f" + reason);
			}
		}
	}

	public static Set<String> getPlayersWithIP(String ip) {
		Set<String> players = new HashSet<String>();
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_players WHERE ip = '" + ip + "'");
		try {
			while (rs.next()) {
				String player = rs.getString("player");
				players.add(player);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}

	public static boolean isIPBanned(String ip) {
		return bannedIPs.contains(ip);
	}

	public static boolean isBanned(String player) {
		return bannedPlayers.containsKey(player.toLowerCase());
	}

	public static boolean isMuted(String string) {
		return mutedPlayers.containsKey(string.toLowerCase());
	}

	public static boolean isBanTemp(String player) {
		if (bannedPlayers.get(player.toLowerCase())) return true;
		return false;
	}

	public static boolean isMuteTemp(String player) {
		if (mutedPlayers.get(player.toLowerCase())) return true;
		return false;

	}

	public static String getBanReason(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("reason");
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMuteReason(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("reason");
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void banPlayer(String player, String reason, String banner) {
		String bandate = getCurrentDate();
		int banlength = 0;
		int unbandate = 0;

		// Adds them to the database.
		DBConnection.sql.modifyQuery("INSERT INTO eb_bans(player, bandate, banlength, unbandate, bannedby, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + bandate + "', "
				+ banlength + ", "
				+ unbandate + ", "
				+ "'" + banner.toLowerCase() + "', "
				+ "'" + reason + "');");
		// Adds them to the HashMap
		bannedPlayers.put(player.toLowerCase(), false);
		// Kicks the player if they are online.
		Player player2 = Bukkit.getPlayer(player);
		if (player2 != null) {
			player2.kickPlayer("§cYou have been banned for: §f" + reason);
		}
		EtriaBans.log.info(player + " has been banned by " + banner);
	}

	public static void tempBanPlayer(String player, String reason, String banner, int length) {
		String bandate = getCurrentDate();
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unbandate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("INSERT INTO eb_bans(player, bandate, banlength, unbandate, bannedby, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + bandate + "', "
				+ "'" + length + "', "
				+ "'" + unbandate + "', "
				+ "'" + banner.toLowerCase() + "', "
				+ "'" + reason + "');");
		bannedPlayers.put(player.toLowerCase(), true);
		Player player2 = Bukkit.getPlayer(player);
		if (player2 != null) {
			player2.kickPlayer("§cYou have been banned for: §f" + reason);
		}
		EtriaBans.log.info(player + " has been temporarily banned by " + banner);
	}

	public static void editBan(String player, int length) {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unbandate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("UPDATE eb_bans SET banlength = " + length + " WHERE player = '" + player.toLowerCase() + "'");
		DBConnection.sql.modifyQuery("UPDATE eb_bans SET unbandate = '" + unbandate + "' WHERE player = '" + player.toLowerCase() + "'");
		bannedPlayers.remove(player.toLowerCase());
		bannedPlayers.put(player.toLowerCase(), true);
	}

	public static void editMute(String player, int length) {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unmutedate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("UPDATE eb_mutes SET mutelength = " + length + " WHERE player = '" + player.toLowerCase() + "'");
		DBConnection.sql.modifyQuery("UPDATE eb_mutes SET unmutedate = " + unmutedate + " WHERE player = '" + player.toLowerCase() + "'");

		mutedPlayers.remove(player.toLowerCase());
		mutedPlayers.put(player.toLowerCase(), true);
	}

	public static void mutePlayer(String player, String reason, String muter) {
		EtriaBans.log.info(player + " has been muted by " + muter);
		mutedPlayers.put(player.toLowerCase(), false);
		DBConnection.sql.modifyQuery("INSERT INTO eb_mutes(player, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + 0 + "', "
				+ "'" + 0 + "', "
				+ "'" + muter.toLowerCase() + "', "
				+ "'" + reason + "');");
		if (Bukkit.getPlayer(player) != null) {
			Bukkit.getPlayer(player).sendMessage("§cYou have been muted for: §f" + reason);
		}
	}

	public static void tempMutePlayer(String player, String reason, String muter, int length) {
		String mutedate = getCurrentDate();
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unmutedate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("INSERT INTO eb_mutes(player, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + mutedate + "', "
				+ "'" + length + "', "
				+ "'" + unmutedate + "', "
				+ "'" + muter.toLowerCase() + "', "
				+ "'" + reason + "');");

		mutedPlayers.put(player.toLowerCase(), true);
		Player player2 = Bukkit.getPlayer(player);
		if (player2 != null) {
			player2.sendMessage("§cYou have been muted for: §f" + reason);
		}
		EtriaBans.log.info(player + " has been temporarily muted by " + muter);
	}

	public static void warnPlayer(String player, String warner, String reason) {
		System.out.println(player + " has been warned by " + warner);
		DBConnection.sql.modifyQuery("INSERT INTO eb_warns(player, date, warner, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + warner.toLowerCase() + "', "
				+ "'" + reason + "');");
		Bukkit.getPlayer(player).sendMessage("§cYou have been warned for: §a" + reason + "§c by §a" + warner);
	}

	public static void kickPlayer(String player, String kicker, String reason) {
		System.out.println(player + " has been kicked by " + kicker);
		DBConnection.sql.modifyQuery("INSERT INTO eb_kicks(player, date, kicker, reason) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + kicker.toLowerCase() + "', "
				+ "'" + reason + "');");
		Bukkit.getPlayer(player).kickPlayer("§cYou have been kicked for: §f" + reason);
	}

	public static void logNewIP(String player, String ip) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_players (player, ip) VALUES ("
				+ "'" + player.toLowerCase() + "', "
				+ "'" + ip + "');");
	}

	public static void updateIP(String player, String ip) {
		DBConnection.sql.modifyQuery("UPDATE eb_players SET ip = '" + ip + "' WHERE player = '" + player.toLowerCase() + "';");
	}

	public static String getLoggedIP(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_players WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("ip");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public static void checkMutes() {
		System.out.println("Checking for expired temp mutes.");
		for (String player: mutedPlayers.keySet()) {
			if (isMuteTemp(player)) {
				Date unbandate = Methods.getUnmuteDate(player);
				Date currentDate = getCurrentDateAsDate();

				long timeUntilUnmute = (unbandate.getTime() - currentDate.getTime());
				if (timeUntilUnmute <= 0) {
					unmutePlayer(player, "CONSOLE");
				}
			}
		}

	}

	public static ResultSet getAllBans() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_bans");
	}

	public static ResultSet getAllMutes() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_mutes");
	}

	public static ResultSet getAllPreviousBans() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans");
	}

	public static ResultSet getAllPreviousMutes() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes");
	}

	public static ResultSet getAllWarns() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_warns");
	}

	public static ResultSet getAllKicks() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_kicks");
	}

	public static ResultSet getAllPlayerData() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_players");
	}

	public static ResultSet getAllIPBans() {
		return DBConnection.sql.readQuery("SELECT * FROM eb_ipbans");
	}

	public static void importData() {

		EtriaBans plugin = EtriaBans.getInstance();

		FileConfiguration data = plugin.getExportedDataConfig();

		final LinkedList<PlayerData> playerData = new LinkedList<PlayerData>(); //
		final LinkedList<BanData> banData = new LinkedList<BanData>(); 
		final LinkedList<MuteData> muteData = new LinkedList<MuteData>();
		final LinkedList<WarnData> warnData = new LinkedList<WarnData>();
		final LinkedList<KickData> kickData = new LinkedList<KickData>();
		final LinkedList<Ban> ban = new LinkedList<Ban>(); //
		final LinkedList<Mute> mute = new LinkedList<Mute>();
		final LinkedList<IPBanData> ipBanData = new LinkedList<IPBanData>();
		if (data.get("players") != null) {
			for (String player: data.getConfigurationSection("players").getKeys(false)) {
				String ip = data.getString("players." + player + ".ip");
				PlayerData pdata = new PlayerData(player, ip);
				playerData.add(pdata);
			}
		}

		if (data.get("previousbans") != null) { // There are previous bans to import
			for (String id: data.getConfigurationSection("previousbans").getKeys(false)) {
				String player = data.getString("previousbans." + id + ".player");
				String bandate = data.getString("previousbans." + id + ".bandate");
				String unbandate = data.getString("previousbans." + id + ".unbandate");
				String bannedby = data.getString("previousbans." + id + ".bannedby");
				String unbannedby = data.getString("previousbans." + id + ".unbannedby");
				String reason = data.getString("previousbans." + id + ".reason");

				BanData bdata = new BanData(player, bandate, unbandate, bannedby, unbannedby, reason);
				banData.add(bdata);
			}
		}

		if (data.get("previousmutes") != null) {
			for (String id: data.getConfigurationSection("previousmutes").getKeys(false)) {
				String player = data.getString("previousmutes." + id + ".player");
				String mutedate = data.getString("previousmutes." + id + ".mutedate");
				String unmutedate = data.getString("previousmutes." + id + ".unmutedate");
				String mutedby = data.getString("previousmutes." + id + ".mutedby");
				String unmutedby = data.getString("previousmutes." + id + ".unmutedby");
				String reason = data.getString("previousmutes." + id + ".reason");

				MuteData mdata = new MuteData(player, mutedate, unmutedate, mutedby, unmutedby, reason);
				muteData.add(mdata);
			}
		}

		if (data.get("ipbans") != null) {
			for (String id: data.getConfigurationSection("ipbans").getKeys(false)) {
				String ip = data.getString("ipbans." + id + ".ip");
				String date = data.getString("ipbans." + id + ".date");
				String reason = data.getString("ipbans." + id + ".reason");
				String bannedby = data.getString("ipbans." + id + ".bannedby");

				IPBanData ipbdata = new IPBanData(ip, date, reason, bannedby);
				ipBanData.add(ipbdata);
			}
		}

		if (data.get("bans") != null) {
			for (String player: data.getConfigurationSection("bans").getKeys(false)) {
				String bandate = data.getString("bans." + player + ".bandate");
				int banlength = data.getInt("bans." + player + ".banlength");
				String unbandate = data.getString("bans." + player + ".unbandate");
				String bannedby = data.getString("bans." + player + ".bannedby");
				String reason = data.getString("bans." + player + ".reason");

				Ban currentBan = new Ban(player, bandate, banlength, unbandate, bannedby, reason);
				ban.add(currentBan);
			}
		}

		if (data.get("mutes") != null) {
			for (String player: data.getConfigurationSection("mutes").getKeys(false)) {
				String mutedate = data.getString("mutes." + player + ".mutedate");
				int mutelength = data.getInt("mutes." + player + ".mutelength");
				String unmutedate = data.getString("mutes." + player + ".unmutedate");
				String mutedby = data.getString("mutes." + player + ".mutedby");
				String reason = data.getString("mutes." + player + ".reason");

				Mute currentMute = new Mute(player, mutedate, mutelength, unmutedate, mutedby, reason);
				mute.add(currentMute);
			}
		}

		if (data.get("warns") != null) {
			for (String player: data.getConfigurationSection("warns").getKeys(false)) {
				String date = data.getString("warns." + player + ".date");
				String warner = data.getString("warns." + player + ".warner");
				String reason = data.getString("warns." + player + ".reason");

				WarnData wData = new WarnData(player, date, warner, reason);
				warnData.add(wData);
			}
		}

		if (data.get("kicks") != null) {
			for (String player: data.getConfigurationSection("kicks").getKeys(false)) {
				String date = data.getString("kicks." + player + ".date");
				String kicker = data.getString("kicks." + player + ".kicker");
				String reason = data.getString("kicks." + player + ".reason");

				KickData kData = new KickData(player, date, kicker, reason);
				kickData.add(kData);
			}
		}

		/*
		 * By this point, all data that needs to be imported is thrown into a linkedlist, so we'll queue up 10 queries at a time until it is done to prevent the server from hanging.
		 */

		importIPBanDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (ipBanData.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importIPBanDataTask);
						Methods.loadIPBans();
						break;
					}

					IPBanData ipData = ipBanData.pop();


					DBConnection.sql.modifyQuery("INSERT INTO eb_ipbans (ip, date, reason, bannedby) VALUES ("
							+ "'" + ipData.getIP() + "', "
							+ "'" + ipData.getDate() + "', "
							+ "'" + ipData.getReason() + "', "
							+ "'" + ipData.getBannedBy() + "');");
					counter++;
				}
			}
		}, 0, 40);

		importCurrentMuteTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (mute.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importCurrentMuteTask);
						Methods.loadMutes();
						break;
					}

					Mute cMute = mute.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_mutes (player, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
							+ "'" + cMute.getPlayer() + "', "
							+ "'" + cMute.getDate() + "', "
							+ cMute.getLength() + ", "
							+ "'" + cMute.getUnmuteDate() + "', "
							+ "'" + cMute.getMutedBy() + "', "
							+ "'" + cMute.getReason() + "');");
					counter++;
				}
			}
		}, 0, 40);

		importkickDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter= 0;
				while (counter < 10) {
					if (kickData.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importkickDataTask);
						break;
					}

					KickData kData = kickData.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_kicks (player, date, kicker, reason) VALUES ("
							+ "'" + kData.getPlayer() + "', "
							+ "'" + kData.getDate() + "', "
							+ "'" + kData.getKicker() + "', "
							+ "'" + kData.getReason() + "');");

					counter++;

				}
			}
		}, 0, 40);

		importWarnDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (warnData.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importWarnDataTask);
						break;
					}

					WarnData wData = warnData.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_warns (player, date, warner, reason) VALUES ("
							+ "'" + wData.getPlayer() + "', "
							+ "'" + wData.getDate() + "', "
							+ "'" + wData.getWarner() + "', "
							+ "'" + wData.getReason() + "');");
					counter++;

				}
			}
		}, 0, 40);
		importMuteDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (muteData.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importMuteDataTask);
						break;
					}
					MuteData prevMute = muteData.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_previous_mutes (player, mutedate, unmutedate, mutedby, unmutedby, reason) VALUES ("
							+ "'" + prevMute.getPlayer() + "', "
							+ "'" + prevMute.getDate() + "', "
							+ "'" + prevMute.getUnmuteDate() + "', "
							+ "'" + prevMute.getMutedBy() + "', "
							+ "'" + prevMute.getUnmutedBy() + "', "
							+ "'" + prevMute.getReason() + "');");
					counter++;
				}
			}
		}, 0, 40);

		importBanDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (banData.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importBanDataTask);
						break;
					}
					BanData prevBan = banData.pop();
					DBConnection.sql.modifyQuery("INSERT INTO eb_previous_bans (player, bandate, unbandate, bannedby, unbannedby, reason) VALUES ("
							+ "'" + prevBan.getPlayer() + "', "
							+ "'" + prevBan.getDate() + "', "
							+ "'" + prevBan.getUnbanDate() + "', "
							+ "'" + prevBan.getBannedBy() + "', "
							+ "'" + prevBan.getUnbannedBy() + "', "
							+ "'" + prevBan.getReason() + "');");
					counter++;
				}
			}
		}, 0, 40);

		importCurrentBanTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (ban.size() == 0) {
						Bukkit.getServer().getScheduler().cancelTask(importCurrentBanTask);
						Methods.loadBans();
						break;
					}

					Ban currentBan = ban.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_bans (player, bandate, banlength, unbandate, bannedby, reason) VALUES ("
							+ "'" + currentBan.getPlayer() + "', "
							+ "'" + currentBan.getDate() + "', "
							+ currentBan.getLength() + ", "
							+ "'" + currentBan.getUnbanDate() + "', "
							+ "'" + currentBan.getBannedBy() + "', "
							+ "'" + currentBan.getReason() + "');");
					counter++;
				}
			}
		}, 0, 40);
		importPlayerDataTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				while (counter < 10) {
					if (playerData.size() == 0) { // Stop repeating
						Bukkit.getServer().getScheduler().cancelTask(importPlayerDataTask);
						break;
					}

					PlayerData data = playerData.pop();

					DBConnection.sql.modifyQuery("INSERT INTO eb_players (player, ip) VALUES ("
							+ "'" + data.getPlayer() + "', "
							+ "'" + data.getIP() + "');");
					counter++;	

				}			
			}
		}, 0, 40);
	}

	public static void exportData() {

		EtriaBans plugin = EtriaBans.getInstance();

		ResultSet bans = Methods.getAllBans();
		ResultSet mutes = Methods.getAllMutes();
		ResultSet previousBans = Methods.getAllPreviousBans();
		ResultSet previousMutes = Methods.getAllPreviousMutes();
		ResultSet warns = Methods.getAllWarns();
		ResultSet kicks = Methods.getAllKicks();
		ResultSet playerData = Methods.getAllPlayerData();
		ResultSet ipBans = Methods.getAllIPBans();

		try {

			if (!playerData.next()) {

			} else {
				do {
					String player = playerData.getString("player");
					String ip = playerData.getString("ip");

					plugin.getExportedDataConfig().set("players." + player + ".ip", ip);
				} while (playerData.next());
			}

			if (!ipBans.next()) {

			} else {
				int id = 1;
				do {
					String ip = ipBans.getString("ip");
					String date = ipBans.getString("date");
					String reason = ipBans.getString("reason");
					String bannedby = ipBans.getString("bannedby");

					plugin.getExportedDataConfig().set("ipbans." + id + ".ip", ip);
					plugin.getExportedDataConfig().set("ipbans." + id + ".date", date);
					plugin.getExportedDataConfig().set("ipbans." + id + ".reason", reason);
					plugin.getExportedDataConfig().set("ipbans." + id + ".bannedby", bannedby);
					id++;
				} while (ipBans.next());
			}

			if (!previousBans.next()) {

			} else {
				int id = 1;
				do {
					String player = previousBans.getString("player");
					String bandate = previousBans.getString("bandate");
					String unbandate = previousBans.getString("unbandate");
					String bannedby = previousBans.getString("bannedby");
					String unbannedby = previousBans.getString("unbannedby");
					String reason = previousBans.getString("reason");

					plugin.getExportedDataConfig().set("previousbans." + id + ".player", player);
					plugin.getExportedDataConfig().set("previousbans." + id + ".bandate", bandate);
					plugin.getExportedDataConfig().set("previousbans." + id + ".unbandate", unbandate);
					plugin.getExportedDataConfig().set("previousbans." + id + ".bannedby", bannedby);
					plugin.getExportedDataConfig().set("previousbans." + id + ".unbannedby", unbannedby);
					plugin.getExportedDataConfig().set("previousbans." + id + ".reason", reason);
					id++;
				} while (previousBans.next());
			}
			if (!previousMutes.next()) {

			} else {
				int id = 1;
				do {
					String player = previousMutes.getString("player");
					String mutedate = previousMutes.getString("mutedate");
					String unmutedate = previousMutes.getString("unmutedate");
					String mutedby = previousMutes.getString("mutedby");
					String unmutedby = previousMutes.getString("unmutedby");
					String reason = previousMutes.getString("reason");

					plugin.getExportedDataConfig().set("previousmutes." + id + ".player", player);
					plugin.getExportedDataConfig().set("previousmutes." + id + ".mutedate", mutedate);
					plugin.getExportedDataConfig().set("previousmutes." + id + ".unmutedate", unmutedate);
					plugin.getExportedDataConfig().set("previousmutes." + id + ".mutedby", mutedby);
					plugin.getExportedDataConfig().set("previousmutes." + id + ".unmutedby", unmutedby);
					plugin.getExportedDataConfig().set("previousmutes." + id + ".reason", reason);
					id++;
				} while (previousMutes.next());
			}
			// Save bans to a file.
			if (!bans.next()) {
				// Do nothing, there are no bans to import.
			} else {

				do {
					String player = bans.getString("player");
					String bandate = bans.getString("bandate");
					int banlength = bans.getInt("banlength");
					String unbandate = bans.getString("unbandate");
					String bannedby = bans.getString("bannedby");
					String reason = bans.getString("reason");

					plugin.getExportedDataConfig().set("bans." + player + ".bandate", bandate);
					plugin.getExportedDataConfig().set("bans." + player + ".banlength", banlength);
					plugin.getExportedDataConfig().set("bans." + player + ".unbandate", unbandate);
					plugin.getExportedDataConfig().set("bans." + player + ".bannedby", bannedby);
					plugin.getExportedDataConfig().set("bans." + player + ".reason", reason);
				} while (bans.next());
			}

			if (!mutes.next()) {
				// Do nothing, there are no mutes to export.
			} else {
				do {
					String player = mutes.getString("player");
					String mutedate = mutes.getString("mutedate");
					int mutelength = mutes.getInt("mutelength");
					String unmutedate = mutes.getString("unmutedate");
					String mutedby = mutes.getString("mutedby");
					String reason = mutes.getString("reason");

					plugin.getExportedDataConfig().set("mutes." + player + ".mutedate", mutedate);
					plugin.getExportedDataConfig().set("mutes." + player + ".mutelength", mutelength);
					plugin.getExportedDataConfig().set("mutes." + player + ".unmutedate", unmutedate);
					plugin.getExportedDataConfig().set("mutes." + player + ".mutedby", mutedby);
					plugin.getExportedDataConfig().set("mutes." + player + ".reason", reason);
				} while (mutes.next());
			}

			if (!warns.next()) {
				// Do nothing
			} else {
				do {
					String player = warns.getString("player");
					String date = warns.getString("date");
					String warner = warns.getString("warner");
					String reason = warns.getString("reason");

					plugin.getExportedDataConfig().set("warns." + player + ".date", date);
					plugin.getExportedDataConfig().set("warns." + player + ".warner", warner);
					plugin.getExportedDataConfig().set("warns." + player + ".reason", reason);
				} while (warns.next());
			}

			if (!kicks.next()) {
				// Do nothing
			} else {
				do {
					String player = kicks.getString("player");
					String date = kicks.getString("date");
					String kicker = kicks.getString("kicker");
					String reason = kicks.getString("reason");

					plugin.getExportedDataConfig().set("kicks." + player + ".date", date);
					plugin.getExportedDataConfig().set("kicks." + player + ".kicker", kicker);
					plugin.getExportedDataConfig().set("kicks." + player + ".reason", reason);
				} while (kicks.next());
			}

			plugin.saveExportedData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkBans() {
		System.out.println("Checking for expired temp bans.");
		for (String player: bannedPlayers.keySet()) {
			if (isBanTemp(player)) {
				Date unbandate = Methods.getUnbanDate(player);
				Date currentDate = Methods.getCurrentDateAsDate();

				long timeUntilUnban = (unbandate.getTime() - currentDate.getTime()); // Returns time
				if (timeUntilUnban <= 0) { // This means they have served their time
					unbanPlayer(player, "CONSOLE");
				}
			}
		}
	}

	public static void unbanPlayer(String player, String unbanner) {
		bannedPlayers.remove(player.toLowerCase()); // Remove from HashMap.

		// Fetch info on the ban:
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs.next()) {
				String bandate = rs.getString("bandate");
				String unbandate = getCurrentDate();
				String banner = rs.getString("bannedby");
				String reason = rs.getString("reason");

				DBConnection.sql.modifyQuery("INSERT INTO eb_previous_bans (player, bandate, unbandate, bannedby, unbannedby, reason) VALUES ("
						+ "'" + player.toLowerCase() + "', "
						+ "'" + bandate + "', "
						+ "'" + unbandate + "', "
						+ "'" + banner.toLowerCase() + "', "
						+ "'" + unbanner + "', "
						+ "'" + reason + "'); ");
				DBConnection.sql.modifyQuery("DELETE FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
				EtriaBans.log.info(player + " has been unbanned by " + unbanner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void unmutePlayer(String player, String unmuter) {
		mutedPlayers.remove(player.toLowerCase());

		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs.next()) {
				String mutedate = rs.getString("mutedate");
				String unmutedate = getCurrentDate();
				String muter = rs.getString("mutedby");
				String reason = rs.getString("reason");

				DBConnection.sql.modifyQuery("INSERT INTO eb_previous_mutes (player, mutedate, unmutedate, mutedby, unmutedby, reason) VALUES ("
						+ "'" + player.toLowerCase() + "', "
						+ "'" + mutedate + "', "
						+ "'" + unmutedate + "', "
						+ "'" + muter.toLowerCase() + "', "
						+ "'" + unmuter.toLowerCase() + "', "
						+ "'" + reason + "'); ");
				DBConnection.sql.modifyQuery("DELETE FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
				EtriaBans.log.info(player + " has been unmuted by " + unmuter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String buildString(String[] args, int start, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			sb.append(args[i]).append(delimiter);
		}
		return sb.toString().trim();
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Date date = new Date();
		return dateFormat.format(date);
	}

	public static Date getCurrentDateAsDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String date2 = dateFormat.format(date);
		try {
			return dateFormat.parse(date2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Date getUnbanDate(String player) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				String unbandate = rs2.getString("unbandate");
				if (unbandate.equals(0)) return null;
				return dateFormat.parse(unbandate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date getUnmuteDate(String player) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				String unmutedate = rs2.getString("unmutedate");
				if (unmutedate.equals(0)) return null;
				return dateFormat.parse(unmutedate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Ban getBan(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				return new Ban(rs2.getString("player"), rs2.getString("bandate"), rs2.getInt("length"), rs2.getString("unbandate"), rs2.getString("bannedby"), rs2.getString("reason"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public static ResultSet getCurrentBan(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
	}
	
	public static Mute getMute(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
		
		try {
			if (rs2.next()) {
				return new Mute(rs2.getString("player"), rs2.getString("mutedate"), rs2.getInt("length"), rs2.getString("unmutedate"), rs2.getString("mutedby"), rs2.getString("reason"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Deprecated
	public static ResultSet getCurrentMute(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE player = '" + player.toLowerCase() + "'");
	}
	
	
	public static int getCurrentBanID(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player.toLowerCase() + "'");
		try {
			if (rs2.next()) {
				return rs2.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static ResultSet getWarnByID(int id) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_warns WHERE id = " + id);
	}
	public static ResultSet getBanByID(int id) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans WHERE id = " + id);
	}

	public static ResultSet getKickByID(int id) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_kicks WHERE id = " + id);
	}

	public static ResultSet getMuteByID(int id) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes WHERE id = " + id);
	}

	@Deprecated
	public static String getCurrentBanDate(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("bandate");
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public static int getCurrentBanLength(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player + "'");
		try {
			if (rs2.next()) {
				return rs2.getInt("banlength");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Deprecated
	public static String getCurrentUnbanDate(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("unbandate");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public static String getCurrentBannedBy(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("bannedby");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Deprecated
	public static String getCurrentBanReason(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + player + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("reason");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static int getTotalBans(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans WHERE player = '" + player.toLowerCase() + "'");
		int bans = 0;
		try {
			while (rs2.next()) {
				bans++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bans;
	}

	public static ResultSet getAllPreviousWarns(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_warns WHERE player = '" + player.toLowerCase() + "'");
	}
	public static ResultSet getAllPreviousBans(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans WHERE player = '" + player.toLowerCase() + "'");
	}

	public static ResultSet getAllPreviousKicks(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_kicks WHERE player = '" + player.toLowerCase() + "'");
	}

	public static ResultSet getAllPreviousMutes(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes WHERE player = '" + player.toLowerCase() + "'");
	}

	public static int getTotalMutes(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes WHERE player = '" + player.toLowerCase() + "'");
		int mutes = 0;
		try {
			while (rs2.next()) {
				mutes++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mutes;
	}

	public static int getTotalWarns(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_warns WHERE player = '" + player.toLowerCase() + "'");
		int warns = 0;
		try {
			while (rs2.next()) {
				warns++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return warns;
	}

	public static int getTotalKicks(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_kicks WHERE player = '" + player.toLowerCase() + "'");
		int kicks = 0;
		try {
			while (rs2.next()) {
				kicks++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return kicks;
	}

}