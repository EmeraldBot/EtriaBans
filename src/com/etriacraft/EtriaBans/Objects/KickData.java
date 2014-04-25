package com.etriacraft.EtriaBans.Objects;

import java.util.UUID;

public class KickData {

	final UUID uuid;
	final String date;
	final String kicker;
	final String reason;

	public KickData(UUID uuid, String date, String kicker, String reason) {
		this.uuid = uuid;;
		this.date = date;
		this.kicker = kicker;
		this.reason = reason;
	}
	
	public UUID getUUID() {
		return this.uuid;
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