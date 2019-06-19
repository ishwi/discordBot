package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.List;

public abstract class MyCommandDbAccess extends MyCommand {
	public ConcurrentLastFM lastFM;
	private DaoImplementation dao;

	public MyCommandDbAccess(DaoImplementation dao) {
		this.dao = dao;
		lastFM = new ConcurrentLastFM();
	}

	String getLastFmUsername1input(String[] message, Long id, MessageReceivedEvent event) throws ParseException {
		String username;
		try {
			if (message.length != 1) {
				username = this.dao.findLastFMData(id).getName();
			} else {
				//Caso con @ y sin @
				List<User> list = event.getMessage().getMentionedUsers();
				username = message[0];
				if (!list.isEmpty()) {
					LastFMData data = this.dao.findLastFMData((list.get(0).getIdLong()));
					username = data.getName();
				}
				if (username.startsWith("@")) {
					event.getChannel().sendMessage("Trolled xD").queue();
				}
			}
		} catch (InstanceNotFoundException e) {
			throw new ParseException("DB");
		}
		return username;
	}

	void userNotOnDB(MessageReceivedEvent e, int code) {

		String message;
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request: ";

		switch (code) {
			case 0:
				message = "User was not found on the database, register first!";
				break;
			default:
				message = "Unknown Error";
				break;
		}
		sendMessage(e, base + message);
	}


	public DaoImplementation getDao() {
		return dao;
	}
}