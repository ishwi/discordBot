package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import DAO.Entities.UsersWrapper;
import main.Exceptions.ParseException;
import main.UpdaterThread;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SetCommand extends ConcurrentCommand {
	public SetCommand(DaoImplementation dao) {
		super(dao);
	}


	@Override
	public void threadableCode() {
		String[] returned;

		try {
			returned = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 0, e1.getMessage());
			return;
		}


		MessageBuilder mes = new MessageBuilder();
		String lastFmID = returned[0];
		long guildID = e.getGuild().getIdLong();
		long userId = e.getAuthor().getIdLong();
		List<UsersWrapper> list = getDao().getAll(guildID);


		Optional<UsersWrapper> u = (list.stream().filter(user -> user.getDiscordID() == userId).findFirst());
		//User was already registered
		if (u.isPresent()) {
			//Registered with different username
			if (!u.get().getLastFMName().equals(lastFmID)) {
				sendMessage(e, "Changing your username, might take a while");
				//Remove pero solo de la guild if no guild remove all
				getDao().remove(userId);
			} else {
				sendMessage(e, e.getAuthor().getName() + " , you are good to go!");
				return;
			}
			//First Time on the guild
		} else {
			//If it was registered in at least other  guild theres no need to update
			if (getDao().getGuildList(userId).stream().anyMatch(user -> user != guildID)) {
				//Adds the user to the guild
				getDao().addGuildUser(userId, guildID);
				sendMessage(e, e.getAuthor().getName() + " , you are good to go!");
				return;

			}
		}


		//Never registered before
		getDao().updateUserLibrary(new LastFMData(lastFmID, userId, guildID));
		mes.setContent("**" + e.getAuthor().getName() + "** has set his last FM name \n Updating his library on the background");
		mes.sendTo(e.getChannel()).queue();

		new Thread(new UpdaterThread(getDao(), new UsersWrapper(userId, lastFmID), false)).run();
		sendMessage(e, "Finished updating " + e.getAuthor().getName() + " library, you are good to go!");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!set");
	}

	@Override
	public String getDescription() {
		return "Adds you to the system";
	}

	@Override
	public String getName() {
		return "Set";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!set lastFMUser\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		String[] message = getSubMessage(e.getMessage());
		if (message.length != 1)
			throw new ParseException("Command");
		return message;
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		if (code == 0) {
			sendMessage(e, base + "You need to introduce a valid LastFm name");
		} else {
			sendMessage(e, base + "You have already set your account");
		}
	}
}
