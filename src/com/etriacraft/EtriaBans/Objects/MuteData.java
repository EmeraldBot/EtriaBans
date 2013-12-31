package com.etriacraft.EtriaBans.Objects;

public class MuteData {
	
	final String player;
	final String mutedate;
	final String unmutedate;
	final String mutedby;
	final String unmutedby;
	final String reason;
	
	public MuteData(String player, String mutedate, String unmutedate, String mutedby, String unmutedby, String reason) {
		this.player = player;
		this.mutedate = mutedate;
		this.unmutedate = unmutedate;
		this.mutedby = mutedby;
		this.unmutedby = unmutedby;
		this.reason = reason;
	}
	
	public String getPlayer() {
		return this.player;
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
