package com.etriacraft.EtriaBans.Objects;

public class PlayerData {

	final String playerName;
	final String ip;
	
	public PlayerData(String playerName, String ip) {
		this.playerName = playerName;
		this.ip = ip;
	}
	
	public String getPlayer() {
		return this.playerName;
	}
	
	public String getIP() {
		return this.ip;
	}
}
