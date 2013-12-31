package com.etriacraft.EtriaBans.Objects;

public class IPBanData {
	
	final String ip;
	final String date;
	final String reason;
	final String bannedby;
	
	public IPBanData(String ip, String date, String reason, String bannedby) {
		this.ip = ip;
		this.date = date;
		this.reason = reason;
		this.bannedby = bannedby;
	}
	
	public String getIP() {
		return this.ip;
	}

	public String getDate() {
		return this.date;
	}
	
	public String getReason() {
		return this.reason;
	}
	
	public String getBannedBy() {
		return this.bannedby;
	}
}
