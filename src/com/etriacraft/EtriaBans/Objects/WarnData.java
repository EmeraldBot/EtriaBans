package com.etriacraft.EtriaBans.Objects;

public class WarnData {

	final String player;
	final String date;
	final String warner;
	final String reason;
	
	public WarnData(String player, String date, String warner, String reason) {
		this.player = player;
		this.date = date;
		this.warner = warner;
		this.reason = reason;
	}
	
	public String getPlayer() {
		return this.player;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getWarner() {
		return this.warner;
	}
	
	public String getReason() {
		return this.reason;
	}

}
