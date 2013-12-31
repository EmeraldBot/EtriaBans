package com.etriacraft.EtriaBans.Objects;

public class Mute {

	final String playerName;
	final String date;
	final long length;
	final String unmutedate;
	final String mutedBy;
	final String reason;
	
	public Mute(String playerName, String date, long length, String unmutedate, String mutedBy, String reason) {
		this.playerName = playerName;
		this.date = date;
		this.length = length;
		this.unmutedate = unmutedate;
		this.mutedBy = mutedBy;
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
	
	public String getUnmuteDate() {
		return this.unmutedate;
	}
	
	public String getMutedBy() {
		return this.mutedBy;
	}
	
	public String getReason() {
		return this.reason;
	}
}
