package com.etriacraft.EtriaBans;

import com.etriacraft.EtriaBans.sql.*;

public final class DBConnection {

	public static Database sql;

	public static String host;
	public static String password;
	public static String database;
	public static String username;
	public static int port;

	public static String engine;

	public static void init() {
		if (engine.equalsIgnoreCase("mysql")) {
			sql = new MySQLConnection(EtriaBans.log, "[EtriaBans] Establishing Database Connection...", host, port, username, password, database);
			((MySQLConnection) sql).open();
			EtriaBans.log.info("Establishing Database Connection...");
			
			if (!sql.tableExists("eb_players")) {
				String query = "CREATE TABLE `eb_players` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`ip` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("eb_bans")) {
				EtriaBans.log.info("Creating eb_bans table.");
				String query = "CREATE TABLE `eb_bans` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`bandate` TEXT(255),"
						+ "`banlength` int(255),"
						+ "`unbandate` TEXT(255),"
						+ "`bannedby` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("eb_mutes")) {
				EtriaBans.log.info("Creating eb_mutes table.");
				String query = "CREATE TABLE `eb_mutes` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`mutedate` TEXT(255),"
						+ "`mutelength` int(255),"
						+ "`unmutedate` TEXT(255),"
						+ "`mutedby` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("eb_previous_bans")) {
				EtriaBans.log.info("CREATING eb_previous_bans table.");
				String query = "CREATE TABLE `eb_previous_bans` ("
						+ " `id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`bandate` TEXT(255),"
						+ "`unbandate` TEXT(255),"
						+ "`bannedby` TEXT(32),"
						+ "`unbannedby` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("eb_previous_mutes")) {
				EtriaBans.log.info("CREATING eb_previous_mutes table.");
				String query = "CREATE TABLE `eb_previous_mutes` ("
						+ " `id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`mutedate` TEXT(255),"
						+ "`unmutedate` TEXT(255),"
						+ "`mutedby` TEXT(32),"
						+ "`unmutedby` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
			if (!sql.tableExists("eb_warns")) {
				EtriaBans.log.info("Creating eb_warns table.");
				String query = "CREATE TABLE `eb_warns` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ "`date` TEXT(255),"
						+ "`warner` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_kicks")) {
				EtriaBans.log.info("Creating eb_kicks table.");
				String query = "CREATE TABLE `eb_kicks` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`player` TEXT(32),"
						+ " `date` TEXT(255),"
						+ "`kicker` TEXT(32),"
						+ "`reason` TEXT(255),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_ipbans")) {
				EtriaBans.log.info("Creating eb_ipbans table.");
				String query = "CREATE TABLE `eb_ipbans` ("
						+ "`id` int(32) NOT NULL AUTO_INCREMENT,"
						+ "`ip` TEXT(255),"
						+ "`date` TEXT(255),"
						+ "`reason` TEXT(255),"
						+ "`bannedby` TEXT(32),"
						+ " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
			
		}
		if (engine.equalsIgnoreCase("sqlite")) {
			sql = new SQLite(EtriaBans.log, "[EtriaBans] Establishing SQLite Connection.", "etriabans.db", EtriaBans.getInstance().getDataFolder().getAbsolutePath());
			((SQLite) sql).open();

			if (!sql.tableExists("eb_players")) {
				EtriaBans.log.info("Creating eb_players table.");
				String query = "CREATE TABLE `eb_players` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`ip` STRING(255));";
				sql.modifyQuery(query);
			}
			if (!sql.tableExists("eb_bans")) {
				EtriaBans.log.info("Creating eb_bans table.");
				String query = "CREATE TABLE `eb_bans` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`bandate` TEXT(255),"
						+ "`banlength` INTEGER(255),"
						+ "`unbandate` TEXT(255),"
						+ "`bannedby` TEXT(32),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_mutes")) {
				EtriaBans.log.info("Creating eb_mutes table.");
				String query = "CREATE TABLE `eb_mutes` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`mutedate` TEXT(255),"
						+ "`mutelength` TEXT(255),"
						+ "`unmutedate` TEXT(255),"
						+ "`mutedby` TEXT(255),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_previous_bans")) {
				EtriaBans.log.info("Creating eb_previous_bans table.");
				String query = "CREATE TABLE `eb_previous_bans` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`bandate` TEXT(255),"
						+ "`unbandate` TEXT(255),"
						+ "`bannedby` TEXT(32),"
						+ "`unbannedby` TEXT(32),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_previous_mutes")) {
				EtriaBans.log.info("Creating eb_previous_mutes table.");
				String query = "CREATE TABLE `eb_previous_mutes` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`mutedate` TEXT(255),"
						+ "`unmutedate` TEXT(255),"
						+ "`mutedby` TEXT(32),"
						+ "`unmutedby` TEXT(32),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_warns")) {
				EtriaBans.log.info("Creating eb_warns table.");
				String query = "CREATE TABLE `eb_warns` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ "`date` TEXT(255),"
						+ "`warner` TEXT(32),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_kicks")) {
				EtriaBans.log.info("Creating eb_kicks table.");
				String query = "CREATE TABLE `eb_kicks` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`player` TEXT(32),"
						+ " `date` TEXT(255),"
						+ "`kicker` TEXT(32),"
						+ "`reason` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("eb_ipbans")) {
				EtriaBans.log.info("Creating eb_ipbans table.");
				String query = "CREATE TABLE `eb_ipbans` ("
						+ "`id` INTEGER PRIMARY KEY,"
						+ "`ip` TEXT(255),"
						+ "`date` TEXT(255),"
						+ "`reason` TEXT(255),"
						+ "`bannedby` TEXT(32));";
				sql.modifyQuery(query);
			}
		} else {
			EtriaBans.log.info("Unknown SQL Engine. Valid options are mysql/sqlite");
		}
	}
}