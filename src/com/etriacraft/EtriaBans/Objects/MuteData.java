package com.etriacraft.EtriaBans.Objects;

import java.util.UUID;

public class MuteData {
	
	final UUID uuid;
	final String mutedate;
	final String unmutedate;
	final String mutedby;
	final String unmutedby;
	final String reason;
	
	public MuteData(UUID uuid, String mutedate, String unmutedate, String mutedby, String unmutedby, String reason) {
		this.uuid = uuid;
		this.mutedate = mutedate;
		this.unmutedate = unmutedate;
		this.mutedby = mutedby;
		this.unmutedby = unmutedby;
		this.reason = reason;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public String getDate() {
		return this.mutedate;
	}
	
	public String getUnmuteDate() {
		return this.unmutedate;
	}
	
	public String getMutedBy() {
		return this.mutedby;
	}
	
	public String getUnmutedBy() {
		return this.unmutedby;
	}
	
	public String getReason() {
		return this.reason;
	}

}
