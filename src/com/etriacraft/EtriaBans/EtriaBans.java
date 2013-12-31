package com.etriacraft.EtriaBans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.etriacraft.EtriaBans.Commands.*;

public class EtriaBans extends JavaPlugin {

	protected static Logger log;
	
	public static EtriaBans instance;
	
	private FileConfiguration exportedData;
	private File exportedDataFile;
	
	@Override
	public void onEnable() {
		exportedDataFile = new File(getDataFolder(), "exportedData.yml");
		
		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		exportedData = new YamlConfiguration();
		
		instance = this;
		EtriaBans.log = this.getLogger();
		
		new Methods(this);
		
		configCheck();
		
		DBConnection.database = getConfig().getString("SQL.database");
		DBConnection.engine = getConfig().getString("SQL.engine");
		DBConnection.host = getConfig().getString("SQL.host");
		DBConnection.password = getConfig().getString("SQL.password");
		DBConnection.port = getConfig().getInt("SQL.port");
		DBConnection.username = getConfig().getString("SQL.username");
		
		DBConnection.init();
		
		new BanCommands(this);
		new IPCommands(this);
		new KickCommands(this);
		new MuteCommands(this);
		new RecordCommands(this);
		new WarnCommands(this);
		new EtriaBansCommand(this);
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		
		Methods.loadBans();
		Methods.loadMutes();
		
		reloadExportedDataConfig();
		Set<String> bannedPlayers = Methods.getBannedPlayers();
		Set<String> mutedPlayers = Methods.getMutedPlayers();
		Set<String> bannedIPs = Methods.getBannedIPs();
		EtriaBans.log.info("Loaded " + mutedPlayers.size() + " muted players.");
		EtriaBans.log.info("Loaded " + bannedPlayers.size() + " player bans.");
		EtriaBans.log.info("Loaded " + bannedIPs.size() + " banned IPs");
		
		loadExtraConfigs();
		
		// This checks our bans / mutes every 5 minutes to make sure none have expired.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				Methods.checkBans();
				Methods.checkMutes();
			}
		}, 0, 6000);
	}
	
	@Override
	public void onDisable() {
		DBConnection.sql.close();
	}
	
	private void firstRun() throws Exception {
		if (!exportedDataFile.exists()) {
			exportedDataFile.getParentFile().mkdirs();
			copy(getResource("exportedData.yml"), exportedDataFile);
		}
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf))>0) {
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static EtriaBans getInstance() {
		return instance;
	}
	
	public void configReload() {
		reloadConfig();
	}
	
	public void configCheck() {
		// Settings
		
		getConfig().addDefault("SQL.engine", "sqlite");
		getConfig().addDefault("SQL.host", "localhost");
		getConfig().addDefault("SQL.password", "password");
		getConfig().addDefault("SQL.database", "minecraft");
		getConfig().addDefault("SQL.username", "root");
		getConfig().addDefault("SQL.port", 3306);
		
		getConfig().addDefault("Settings.CheckIPOnLogin", true);
		getConfig().addDefault("Settings.CanOnlyUnbanOwnBans", true);
		getConfig().addDefault("Settings.CanOnlyUnmuteOwnMutes", true);
		
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void saveExportedData() {
		if (exportedData == null || exportedDataFile == null) {
			return;
		}
		try {
			getExportedDataConfig().save(exportedDataFile);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not save exportedData.yml file.");
		}
	}
	
	public FileConfiguration getExportedDataConfig() {
		if (exportedData == null) {
			reloadExportedDataConfig();
		}
		
		return exportedData;
	}
	
	public void loadExtraConfigs() {
		try {
			exportedData.load(exportedDataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void reloadExportedDataConfig() {
		if (exportedDataFile == null) {
			EtriaBans.log.info("exportedData.yml not found, generation.");
			exportedDataFile = new File(getDataFolder(), "exportedData.yml");
		}
		
		exportedData = YamlConfiguration.loadConfiguration(exportedDataFile);
		
		InputStream defConfigStream = this.getResource("exportedData.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			exportedData.setDefaults(defConfig);
		}
	}
}
