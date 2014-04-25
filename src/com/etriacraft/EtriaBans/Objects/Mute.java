package com.etriacraft.EtriaBans.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.etriacraft.EtriaBans.DBConnection;

public class Mute {

	final UUID uuid;
	final String date;
	final long length;
	final String unmutedate;
	final String mutedBy;
	final String reason;
	
	public Mute(UUID uuid, String date, long length, String unmutedate, String mutedBy, String reason) {
		this.uuid = uuid;
		this.date = date;
		this.length = length;
		this.unmutedate = unmutedate;
		this.mutedBy = mutedBy;
		this.reason = reason;
	}
	
	public int getID() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM eb_mutes WHERE uuid = '" + this.uuid.toString() + "'");
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
