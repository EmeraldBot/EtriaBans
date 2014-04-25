package com.etriacraft.EtriaBans.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.etriacraft.EtriaBans.EtriaBans;
import com.etriacraft.EtriaBans.Methods;
import com.etriacraft.EtriaBans.Objects.IPBanData;

public class IPCommands {
	
	EtriaBans plugin;
	
	public IPCommands(EtriaBans instance) {
		this.plugin = instance;
		init();
	}
	
	private void init() {
		PluginCommand banip = plugin.getCommand("banip");
		PluginCommand checkip = plugin.getCommand("checkip");
		CommandExecutor exe;
		
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.banip")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				
				if (args.length < 2) {
					s.sendMessage("§3Proper Usage: §6/banip [IP Address ] [Reason]");
					return true;
				}
				
				String ip = args[0];
				String reason = Methods.buildString(args, 1, " ");
				
				if (Methods.isIPBanned(ip)) {
					s.sendMessage("§cYou cannot ban an IP that is already banned.");
					return true;
				}
				
				if (((Player) s).getAddress().equals(ip)) {
					s.sendMessage("§cYou cannot IP ban yourself.");
					return true;
				}
				
				IPBanData ipBanData = new IPBanData(ip, Methods.getCurrentDate(), reason, s.getName().toLowerCase());
				Methods.banIP(ipBanData);
				return true;
			}
		}; banip.setExecutor(exe);
		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("etriabans.checkip")) {
					s.sendMessage("§cYou don't have permission to do that.");
					return true;
				}
				
				if (args.length != 1) {
					s.sendMessage("§3Proper Usage: §6/checkip [IP Address]");
					return true;
				}
				
				String ip = args[0];
				s.sendMessage("§aOther Players With IP Address: §6" + ip);
				s.sendMessage("§3" + Methods.getPlayersWithIP(ip).toString());
				return true;
			}
		}; checkip.setExecutor(exe);
		
	}

}
