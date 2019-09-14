package dao.entities;

import main.commands.CommandUtil;

public class CrownsLbEntry extends LbEntry {

	public CrownsLbEntry(String user, long discordId, int entryCount) {
		super(user, discordId, entryCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
				") - " + getEntryCount() +
				" crowns\n";
	}

}