package com.etriacraft.EtriaBans.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.etriacraft.EtriaBans.DBConnection;

public class Ban {

	final UUID uuid;
	final String date;
	final long length;
	final String unbandate;
	final String bannedBy;
	final String reason;
	
	public Ban(UUID uuid, String date, long length, String unbandate, String bannedBy, String reason) {
		this.uuid = uuid;
		this.date = date;
		this.length = length;
		this.unbandate = unbandate;
		this.bannedBy = bannedBy;
		this.reason = reason;
	}
	
	public int getID() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_bans WHERE uuid = '" + this.uuid + "'");
		try {
			if (rs2.next()) {
				return rs2.getInt("id");
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public UUID getUUID() {
		return this.uuid;
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
