package com.etriacraft.EtriaBans.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;

public class EtriaBansCommand {
	
	EtriaBans plugin;
	
	public EtriaBansCommand(EtriaBans instance) {
		this.plugin = instance;
		init();
	}
	
	private void init() {
		PluginCommand etriabans = plugin.getCommand("etriabans");
		CommandExecutor exe;
		
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					if (s.hasPermission("etriabans.info")) {
						s.sendMessage("§3/eb info§f - Displays info about the plugin");
					}
					if (s.hasPermission("etriabans.export")) {
						s.sendMessage("§3/eb export§f - Export all EtriaBans data to a file.");
					}
					if (s.hasPermission("etriabans.import")) {
						s.sendMessage("§3/eb import§f - Import all data from exportedData.yml");
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("info")) {
					if (!s.hasPermission("etriabans.info")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					s.sendMessage("§aYou are running EtriaBans version: §7" + plugin.getDescription().getVersion() + "§a.");
					s.sendMessage("§aCreated by: §7MistPhizzle§a.");
					return true;
				}
				if (args[0].equalsIgnoreCase("import")) {
					if (!s.hasPermission("etriabans.import")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					s.sendMessage("§aData Import Began, this may take a few minutes based on how large your exportedData.yml file is.");
					Methods.importData();
					return true;
				}
				if (args[0].equalsIgnoreCase("export")) {
					if (!s.hasPermission("etriabans.export")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					Methods.exportData();
					s.sendMessage("§aData exported to file.");
					return true;
				}
				return true;
			}
		}; etriabans.setExecutor(exe);
	}

}
