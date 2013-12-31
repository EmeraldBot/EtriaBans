package com.etriacraft.EtriaBans.Objects;

public class BanData {

	final String playerName;
	final String bandate;
	final String unbandate;
	final String bannedby;
	final String unbannedby;
	final String reason;
	
	public BanData(String playerName, String bandate, String unbandate, String bannedby, String unbannedby, String reason) {
		this.playerName = playerName;
		this.bandate = bandate;
		this.unbandate = unbandate;
		this.bannedby = bannedby;
		this.unbannedby = unbannedby;
		this.reason = reason;
	}
	
	public String getPlayer() {
		return this.playerName;
	}
	
	public String getDate() {
		return this.bandate;
	}
	
	public String getUnbanDate() {
		return this.unbandate;
	}
	
	public String getBannedBy() {
		return this.bannedby;
	}
	
	public String getUnbannedBy() {
		return this.unbannedby;
	}
	
	public String getReason() {
		return this.reason;
	}

}
