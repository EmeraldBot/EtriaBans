package com.etriacraft.EtriaBans.Objects;

import java.util.UUID;

public class WarnData {

	final UUID uuid;
	final String date;
	final String warner;
	final String reason;
	
	public WarnData(UUID uuid, String date, String warner, String reason) {
		this.uuid = uuid;
		this.date = date;
		this.warner = warner;
		this.reason = reason;
	}
	
	public UUID getUUID() {
		return this.uuid;
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
