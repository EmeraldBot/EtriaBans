package com.etriacraft.EtriaBans.Objects;

import java.util.UUID;

public class PlayerData {

	final UUID uuid;
	final String playerName;
	final String ip;
	
	public PlayerData(UUID uuid, String playerName, String ip) {
		this.uuid = uuid;
		this.playerName = playerName;
		this.ip = ip;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public String getPlayer() {
		return this.playerName;
	}
	
	public String getIP() {
		return this.ip;
	}
}
