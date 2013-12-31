package com.etriacraft.EtriaBans.Objects;

public class KickData {

	final String player;
	final String date;
	final String kicker;
	final String reason;

	public KickData(String player, String date, String kicker, String reason) {
		this.player = player;
		this.date = date;
		this.kicker = kicker;
		this.reason = reason;
	}
	
	public String getPlayer() {
		return this.player;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getKicker() {
		return this.kicker;
	}
	
	public String getReason() {
		return this.reason;
	}
}