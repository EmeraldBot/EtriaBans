package com.etriacraft.EtriaBans.Objects;

public class Ban {

	final String playerName;
	final String date;
	final long length;
	final String unbandate;
	final String bannedBy;
	final String reason;
	
	public Ban(String playerName, String date, long length, String unbandate, String bannedBy, String reason) {
		this.playerName = playerName;
		this.date = date;
		this.length = length;
		this.unbandate = unbandate;
		this.bannedBy = bannedBy;
		this.reason = reason;
	}
	
	public String getPlayer() {
		return this.playerName;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public long getLength() {
		return this.length;
	}
	
	public String getUnbanDate() {
		return this.unbandate;
	}
	
	public String getBannedBy() {
		return this.bannedBy;
	}
	
	public String getReason() {
		return this.reason;
	}
	
	
}
