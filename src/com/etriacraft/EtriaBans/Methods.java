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
import java.util.UUID;

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

	public static HashMap<String, Boolean> bannedPlayers = new HashMap<String, Boolean>(); // {uuid, isTemporary}
	public static HashMap<String, Boolean> mutedPlayers = new HashMap<String, Boolean>(); // {uuid, isTemporary}
	public static Set<IPBanData> bannedIPs = new HashSet<IPBanData>();

	public static void loadIPBans() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_ipbans");
		try {
			while (rs.next()) {
				IPBanData ipBan = new IPBanData(rs.getString("ip"), rs.getString("date"), rs.getString("reason"), rs.getString("bannedby"));
				bannedIPs.add(ipBan);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void loadBans() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_bans");
		try {
			while (rs.next()) {
				String player = rs.getString("uuid");
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

	public static Set<IPBanData> getBannedIPs() {
		return bannedIPs;
	}

	public static void loadMutes() {
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_mutes");
		try {
			while (rs.next()) {
				String player = rs.getString("uuid");
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

	public static void banIP(IPBanData ban) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_ipbans (ip, date, reason, bannedby) VALUES ("
				+ "'" + ban.getIP() + "', "
				+ "'" + ban.getDate() + "', "
				+ "'" + ban.getReason() +  "', "
				+ "'" + ban.getBannedBy() + "');");
		bannedIPs.add(ban);
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (player.getAddress().getAddress().getHostAddress().equals(ban.getIP()) && !player.hasPermission("etriabans.exempt.bans")) {
				player.kickPlayer("§cThis IP has been banned for: §f" + ban.getReason());
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
		for (IPBanData data: bannedIPs) {
			if (data.getIP().equalsIgnoreCase(ip)) return true;
		}
		return false;
	}

	public static boolean isBanned(UUID uuid) {
		return bannedPlayers.containsKey(uuid.toString());
	}

	public static boolean isMuted(UUID uuid) {
		return mutedPlayers.containsKey(uuid.toString());
	}

	public static boolean isBanTemp(UUID uuid) {
		if (bannedPlayers.get(uuid.toString())) return true;
		return false;
	}

	public static boolean isMuteTemp(UUID uuid) {
		if (mutedPlayers.get(uuid.toString())) return true;
		return false;

	}

	public static String getBanReason(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE uuid = '" + uuid.toString() + "'");
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

	public static String getMuteReason(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE uuid = '" + uuid.toString() + "'");
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

	public static void banPlayer(UUID uuid, String reason, String banner) {
		String bandate = getCurrentDate();
		int banlength = 0;
		int unbandate = 0;

		// Adds them to the database.
		DBConnection.sql.modifyQuery("INSERT INTO eb_bans(uuid, bandate, banlength, unbandate, bannedby, reason) VALUES ("
				+ "'" + uuid + "', "
				+ "'" + bandate + "', "
				+ banlength + ", "
				+ "'" + unbandate + "', "
				+ "'" + banner.toLowerCase() + "', "
				+ "'" + reason + "');");
		// Adds them to the HashMap
		bannedPlayers.put(uuid.toString(), false);
		// Kicks the player if they are online.
		Player player2 = Bukkit.getPlayer(uuid);
		if (player2 != null) {
			player2.kickPlayer("§cYou have been banned for: §f" + reason);
			EtriaBans.log.info(player2.getName() + " has been banned by " + banner);
		} else {
			EtriaBans.log.info(uuid.toString() + " has been banned by " + banner);
		}

	}

	public static void tempBanPlayer(UUID uuid, String reason, String banner, int length) {
		String bandate = getCurrentDate();
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unbandate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("INSERT INTO eb_bans(uuid, bandate, banlength, unbandate, bannedby, reason) VALUES ("
				+ "'" +uuid.toString() + "', "
				+ "'" + bandate + "', "
				+ length + ", "
				+ "'" + unbandate + "', "
				+ "'" + banner.toLowerCase() + "', "
				+ "'" + reason + "');");
		bannedPlayers.put(uuid.toString(), true);
		Player player2 = Bukkit.getPlayer(uuid);
		if (player2 != null) {
			player2.kickPlayer("§cYou have been banned for: §f" + reason);
			EtriaBans.log.info(player2.getName() + " has been temporarily banned by " + banner);
		} else {
			EtriaBans.log.info(uuid.toString() + " has been temporarily banned by " + banner);
		}
	}

	public static void editBan(UUID uuid, int length) {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unbandate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("UPDATE eb_bans SET banlength = " + length + " WHERE uuid = '" + uuid.toString() + "'");
		DBConnection.sql.modifyQuery("UPDATE eb_bans SET unbandate = '" + unbandate + "' WHERE uuid = '" + uuid.toString() + "'");
		bannedPlayers.remove(uuid.toString());
		bannedPlayers.put(uuid.toString(), true);
	}

	public static void editMute(UUID uuid, int length) {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unmutedate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("UPDATE eb_mutes SET mutelength = " + length + " WHERE uuid = '" + uuid.toString() + "'");
		DBConnection.sql.modifyQuery("UPDATE eb_mutes SET unmutedate = " + unmutedate + " WHERE uuid = '" + uuid.toString() + "'");

		mutedPlayers.remove(uuid.toString());
		mutedPlayers.put(uuid.toString(), true);
	}

	public static void mutePlayer(UUID uuid, String reason, String muter) {
		mutedPlayers.put(uuid.toString(), false);
		DBConnection.sql.modifyQuery("INSERT INTO eb_mutes(uuid, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
				+ "'" + uuid.toString() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + 0 + "', "
				+ "'" + 0 + "', "
				+ "'" + muter.toLowerCase() + "', "
				+ "'" + reason + "');");
		if (Bukkit.getPlayer(uuid) != null) {
			Bukkit.getPlayer(uuid).sendMessage("§cYou have been muted for: §f" + reason);
			EtriaBans.log.info(Bukkit.getPlayer(uuid).getName() + " has been muted for " + reason);
		} else {
			EtriaBans.log.info(uuid + " has been muted for " + reason);
		}
	}

	public static void tempMutePlayer(String uuid, String reason, String muter, int length) {
		String mutedate = getCurrentDate();
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		cal.add(Calendar.SECOND, length);
		String unmutedate = dateFormat.format(cal.getTime());

		DBConnection.sql.modifyQuery("INSERT INTO eb_mutes(uuid, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
				+ "'" + uuid.toString() + "', "
				+ "'" + mutedate + "', "
				+ "'" + length + "', "
				+ "'" + unmutedate + "', "
				+ "'" + muter.toLowerCase() + "', "
				+ "'" + reason + "');");

		mutedPlayers.put(uuid.toString(), true);
		Player player2 = Bukkit.getPlayer(uuid);
		if (player2 != null) {
			player2.sendMessage("§cYou have been muted for: §f" + reason);
			EtriaBans.log.info(player2.getName() + " has been temporarily muted by " + muter);
		} else {
			EtriaBans.log.info(uuid + " has been temporarily muted by " + muter);
		}
	}

	public static void warnPlayer(UUID uuid, String warner, String reason) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_warns(uuid, date, warner, reason) VALUES ("
				+ "'" + uuid.toString() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + warner.toLowerCase() + "', "
				+ "'" + reason + "');");
		Bukkit.getPlayer(uuid).sendMessage("§cYou have been warned for: §a" + reason + "§c by §a" + warner);
		EtriaBans.log.info(Bukkit.getPlayer(uuid) + " has been warned by " + warner);
	}

	public static void kickPlayer(UUID uuid, String kicker, String reason) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_kicks(uuid, date, kicker, reason) VALUES ("
				+ "'" + uuid.toString() + "', "
				+ "'" + getCurrentDate() + "', "
				+ "'" + kicker.toLowerCase() + "', "
				+ "'" + reason + "');");
		Bukkit.getPlayer(uuid).kickPlayer("§cYou have been kicked for: §f" + reason);
		EtriaBans.log.info(Bukkit.getPlayer(uuid).getName() + " has been kicked for " + reason);
	}

	public static void logNewIP(UUID uuid, String player, String ip) {
		DBConnection.sql.modifyQuery("INSERT INTO eb_players (uuid, player, ip) VALUES ("
				+ "'" + uuid.toString() + "', "
				+ "'" + player.toLowerCase() + "', "
				+ "'" + ip + "');");
	}

	public static void updateIP(UUID uuid, String ip) {
		DBConnection.sql.modifyQuery("UPDATE eb_players SET ip = '" + ip + "' WHERE uuid = '" + uuid.toString() + "';");
	}

	public static String getLoggedIP(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_players WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (rs2.next()) {
				return rs2.getString("ip");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Deprecated
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
		for (String id: mutedPlayers.keySet()) {
			UUID uuid = UUID.fromString(id);
			if (isMuteTemp(uuid)) {
				Date unbandate = Methods.getUnmuteDate(uuid);
				Date currentDate = getCurrentDateAsDate();

				long timeUntilUnmute = (unbandate.getTime() - currentDate.getTime());
				if (timeUntilUnmute <= 0) {
					unmutePlayer(uuid, "CONSOLE");
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
			for (String uuid: data.getConfigurationSection("players").getKeys(false)) {
				String ip = data.getString("players." + uuid + ".ip");
				String name = data.getString("players." + uuid + ".name");
				PlayerData pdata = new PlayerData(UUID.fromString(uuid), name, ip);
				playerData.add(pdata);
			}
		}

		if (data.get("previousbans") != null) { // There are previous bans to import
			for (String id: data.getConfigurationSection("previousbans").getKeys(false)) {
				String uuid = data.getString("previousbans." + id + ".uuid");
				String bandate = data.getString("previousbans." + id + ".bandate");
				String unbandate = data.getString("previousbans." + id + ".unbandate");
				String bannedby = data.getString("previousbans." + id + ".bannedby");
				String unbannedby = data.getString("previousbans." + id + ".unbannedby");
				String reason = data.getString("previousbans." + id + ".reason");

				BanData bdata = new BanData(UUID.fromString(uuid), bandate, unbandate, bannedby, unbannedby, reason);
				banData.add(bdata);
			}
		}

		if (data.get("previousmutes") != null) {
			for (String id: data.getConfigurationSection("previousmutes").getKeys(false)) {
				String player = data.getString("previousmutes." + id + ".uuid");
				String mutedate = data.getString("previousmutes." + id + ".mutedate");
				String unmutedate = data.getString("previousmutes." + id + ".unmutedate");
				String mutedby = data.getString("previousmutes." + id + ".mutedby");
				String unmutedby = data.getString("previousmutes." + id + ".unmutedby");
				String reason = data.getString("previousmutes." + id + ".reason");

				MuteData mdata = new MuteData(UUID.fromString(player), mutedate, unmutedate, mutedby, unmutedby, reason);
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

				Ban currentBan = new Ban(UUID.fromString(player), bandate, banlength, unbandate, bannedby, reason);
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

				Mute currentMute = new Mute(UUID.fromString(player), mutedate, mutelength, unmutedate, mutedby, reason);
				mute.add(currentMute);
			}
		}

		if (data.get("warns") != null) {
			for (String player: data.getConfigurationSection("warns").getKeys(false)) {
				String date = data.getString("warns." + player + ".date");
				String warner = data.getString("warns." + player + ".warner");
				String reason = data.getString("warns." + player + ".reason");

				WarnData wData = new WarnData(UUID.fromString(player), date, warner, reason);
				warnData.add(wData);
			}
		}

		if (data.get("kicks") != null) {
			for (String player: data.getConfigurationSection("kicks").getKeys(false)) {
				String date = data.getString("kicks." + player + ".date");
				String kicker = data.getString("kicks." + player + ".kicker");
				String reason = data.getString("kicks." + player + ".reason");

				KickData kData = new KickData(UUID.fromString(player), date, kicker, reason);
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_mutes (uuid, mutedate, mutelength, unmutedate, mutedby, reason) VALUES ("
							+ "'" + cMute.getUUID().toString() + "', "
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_kicks (uuid, date, kicker, reason) VALUES ("
							+ "'" + kData.getUUID().toString() + "', "
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_warns (uuid, date, warner, reason) VALUES ("
							+ "'" + wData.getUUID().toString() + "', "
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_previous_mutes (uuid, mutedate, unmutedate, mutedby, unmutedby, reason) VALUES ("
							+ "'" + prevMute.getUUID().toString() + "', "
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
					DBConnection.sql.modifyQuery("INSERT INTO eb_previous_bans (uuid, bandate, unbandate, bannedby, unbannedby, reason) VALUES ("
							+ "'" + prevBan.getUUID().toString() + "', "
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_bans (uuid, bandate, banlength, unbandate, bannedby, reason) VALUES ("
							+ "'" + currentBan.getUUID().toString() + "', "
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

					DBConnection.sql.modifyQuery("INSERT INTO eb_players (uuid, player, ip) VALUES ("
							+ "'" + data.getUUID().toString() + "', "
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
					String uuid = playerData.getString("uuid");
					String player = playerData.getString("player");
					String ip = playerData.getString("ip");

					plugin.getExportedDataConfig().set("players." + uuid + "." + ".name", player);
					plugin.getExportedDataConfig().set("players." + uuid + "." + ".ip", ip);
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
					String player = previousBans.getString("uuid");
					String bandate = previousBans.getString("bandate");
					String unbandate = previousBans.getString("unbandate");
					String bannedby = previousBans.getString("bannedby");
					String unbannedby = previousBans.getString("unbannedby");
					String reason = previousBans.getString("reason");

					plugin.getExportedDataConfig().set("previousbans." + id + ".uuid", player);
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
					String player = previousMutes.getString("uuid");
					String mutedate = previousMutes.getString("mutedate");
					String unmutedate = previousMutes.getString("unmutedate");
					String mutedby = previousMutes.getString("mutedby");
					String unmutedby = previousMutes.getString("unmutedby");
					String reason = previousMutes.getString("reason");

					plugin.getExportedDataConfig().set("previousmutes." + id + ".uuid", player);
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
					String player = bans.getString("uuid");
					String bandate = bans.getString("bandate");
					int banlength = bans.getInt("banlength");
					String unbandate = bans.getString("unbandate");
					String bannedby = bans.getString("bannedby");
					String reason = bans.getString("reason");

					plugin.getExportedDataConfig().set("bans." + player + ".uuid", bandate);
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
					String player = mutes.getString("uuid");
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
					String player = warns.getString("uuid");
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
					String player = kicks.getString("uuid");
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
		for (String id: bannedPlayers.keySet()) {
			UUID uuid = UUID.fromString(id);
			if (isBanTemp(uuid)) {
				Date unbandate = Methods.getUnbanDate(uuid);
				Date currentDate = Methods.getCurrentDateAsDate();

				long timeUntilUnban = (unbandate.getTime() - currentDate.getTime()); // Returns time
				if (timeUntilUnban <= 0) { // This means they have served their time
					unbanPlayer(uuid, "CONSOLE");
				}
			}
		}
	}

	public static void unbanPlayer(UUID uuid, String unbanner) {
		bannedPlayers.remove(uuid.toString()); // Remove from HashMap.

		// Fetch info on the ban:
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (rs.next()) {
				String bandate = rs.getString("bandate");
				String unbandate = getCurrentDate();
				String banner = rs.getString("bannedby");
				String reason = rs.getString("reason");

				DBConnection.sql.modifyQuery("INSERT INTO eb_previous_bans (uuid, bandate, unbandate, bannedby, unbannedby, reason) VALUES ("
						+ "'" + uuid.toString() + "', "
						+ "'" + bandate + "', "
						+ "'" + unbandate + "', "
						+ "'" + banner.toLowerCase() + "', "
						+ "'" + unbanner + "', "
						+ "'" + reason + "'); ");
				DBConnection.sql.modifyQuery("DELETE FROM eb_bans WHERE uuid = '" + uuid.toString() + "'");
				EtriaBans.log.info(uuid + " has been unbanned by " + unbanner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void unmutePlayer(UUID uuid, String unmuter) {
		mutedPlayers.remove(uuid.toString());

		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (rs.next()) {
				String mutedate = rs.getString("mutedate");
				String unmutedate = getCurrentDate();
				String muter = rs.getString("mutedby");
				String reason = rs.getString("reason");

				DBConnection.sql.modifyQuery("INSERT INTO eb_previous_mutes (uuid, mutedate, unmutedate, mutedby, unmutedby, reason) VALUES ("
						+ "'" + uuid.toString() + "', "
						+ "'" + mutedate + "', "
						+ "'" + unmutedate + "', "
						+ "'" + muter.toLowerCase() + "', "
						+ "'" + unmuter.toLowerCase() + "', "
						+ "'" + reason + "'); ");
				DBConnection.sql.modifyQuery("DELETE FROM eb_mutes WHERE uuid = '" + uuid.toString() + "'");
				EtriaBans.log.info(uuid + " has been unmuted by " + unmuter);
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

	public static Date getUnbanDate(UUID uuid) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE uuid = '" + uuid.toString() + "'");
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

	public static Date getUnmuteDate(UUID uuid) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE uuid = '" + uuid.toString() + "'");
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

	public static Ban getBan(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE player = '" + uuid.toString() + "'");
		try {
			if (rs2.next()) {
				return new Ban((UUID) rs2.getObject("uuid"), rs2.getString("bandate"), rs2.getInt("banlength"), rs2.getString("unbandate"), rs2.getString("bannedby"), rs2.getString("reason"));
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
	
	public static Mute getMute(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE uuid = '" + uuid.toString() + "'");
		
		try {
			if (rs2.next()) {
				return new Mute((UUID) rs2.getObject("uuid"), rs2.getString("mutedate"), rs2.getInt("mutelength"), rs2.getString("unmutedate"), rs2.getString("mutedby"), rs2.getString("reason"));
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
	
	
	public static int getCurrentBanID(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE uuid = '" + uuid.toString() + "'");
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


	public static int getTotalBans(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans WHERE player = '" + uuid.toString() + "'");
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

	public static ResultSet getAllPreviousWarns(UUID uuid) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_warns WHERE uuid = '" + uuid.toString() + "'");
	}
	public static ResultSet getAllPreviousBans(UUID uuid) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_bans WHERE uuid = '" + uuid.toString() + "'");
	}

	public static ResultSet getAllPreviousKicks(UUID uuid) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_kicks WHERE uuid = '" + uuid.toString() + "'");
	}

	public static ResultSet getAllPreviousMutes(UUID uuid) {
		return DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes WHERE uuid = '" + uuid.toString() + "'");
	}

	public static int getTotalMutes(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_previous_mutes WHERE uuid = '" + uuid.toString() + "'");
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

	public static int getTotalWarns(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_warns WHERE uuid = '" + uuid.toString() + "'");
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

	public static int getTotalKicks(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_kicks WHERE uuid = '" + uuid.toString() + "'");
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