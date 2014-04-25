package com.etriacraft.EtriaBans.Objects;

import java.util.UUID;

public class BanData {

	final UUID uuid;
	final String bandate;
	final String unbandate;
	final String bannedby;
	final String unbannedby;
	final String reason;
	
	public BanData(UUID uuid, String bandate, String unbandate, String bannedby, String unbannedby, String reason) {
		this.uuid = uuid;
		this.bandate = bandate;
		this.unbandate = unbandate;
		this.bannedby = bannedby;
		this.unbannedby = unbannedby;
		this.reason = reason;
	}
	
	public UUID getUUID() {
		return this.uuid;
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
