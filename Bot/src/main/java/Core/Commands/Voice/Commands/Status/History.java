package Core.Commands.Voice.Commands.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Commands.Status.History.HistorySlashCommand;
import Core.Commands.Voice.MusicCommand;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;

@SubCommand( value = HistorySlashCommand.class )
public class History extends MusicCommand
{
	@Command
	public static class HistorySlashCommand implements ISlashCommand
	{
		@Override
		public String commandName()
		{
			return "music-history";
		}

		@Override
		public String getDescription(){
			return "Show recently played music";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }
	}

	public static ArrayList<AudioObject> getTracks(Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		String text = String.join(" ", args);
		int num = -1;

		if (Utils.isInteger(text)) {
			num = Integer.parseInt(text) - 1;
		}

		ArrayList<AudioObject> tracks = new ArrayList<>(MusicCommand.getUserHistory(guild, author));


		if (num != -1) {
			if (num > 0 && tracks.size() > num) {
				tracks.remove(num - 1);
			}
		}

		if (num == -1) {
			tracks.removeIf((e) -> text.contains(e.url) || text.equalsIgnoreCase(e.url) || e.name.contains(
					text) && text.length() >= 3 || e.name.equalsIgnoreCase(text) || StringUtils.difference(
					e.name.toLowerCase(), text.toLowerCase()).length() <= 5);
		}

		return tracks;
	}
	@Override
	public String getDescription()
	{
		return "Shows you last " + MusicCommand.maxHistory + " played songs on this server";
	}

	@Override
	public String commandName()
	{
		return "view";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ArrayList<AudioObject> objects = new ArrayList<>(MusicCommand.getUserHistory(guild, author));

		if (objects.size() == 0) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no history to show!");
			return;
		}

		Collections.reverse(objects);

		EmbedBuilder builder = new EmbedBuilder();
		StringBuilder bd = new StringBuilder();

		int i = 1;

		for (AudioObject ob : objects) {
			StringBuilder bd1 = new StringBuilder();
			bd1.append("**").append(i).append("**) [**").append(ob.name).append("**](").append(ob.url).append(")\n");

			if ((bd1.length() + bd.length()) > (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
				int k = (objects.size() - i);
				if(k > 0) bd.append("And ").append(k).append(" more.");
				break;
			} else {
				bd.append(bd1);
			}

			i++;
		}

		builder.setTitle(
				"**Show last " + (Math.min(objects.size(), maxHistory)) + " songs played**");
		builder.setDescription(bd.toString());

		ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
	}

	@SubCommand( value = HistorySlashCommand.class)
	public static class removeHistory implements ISlashCommand
	{
		@SlashArgument( name = "remove", description = "Which tracks you want to remove from your history", required = true )
		public String removeTarget;

		@Override
		public String getDescription()
		{
			return "Remove a specific track from your history";
		}

		@Override
		public String commandName()
		{
			return "remove";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ArrayList<AudioObject> tracks = getTracks(guild, channel, author, message, new String[]{removeTarget});
			ArrayList<AudioObject> tracks1 = MusicCommand.getUserHistory(guild, author);
			tracks1.removeAll(tracks);

			MusicCommand.trackHistory.get(guild.getIdLong()).put(author.getIdLong(), tracks);

			int j = tracks.size();

			if (j > 0) {
				EmbedBuilder builder = new EmbedBuilder();
				StringBuilder bd = new StringBuilder();

				int i = 0;
				for (AudioObject track : tracks1) {
					StringBuilder bd1 = new StringBuilder();

					String title = track.name;

					if (title.contains("[") && !title.contains("]")) {
						title += "]";
					}
					if (title.length() > 75) {
						title = title.substring(0, 75);
					}

					bd1.append("[**").append(title).append("**](").append(track.url).append(")");

					if (bd1.length() + bd.length() > (MessageEmbed.VALUE_MAX_LENGTH - 80)) {
						bd.append("And ").append(tracks.size() - i).append(" more");
						break;
					} else {
						bd.append(bd1).append("\n");
					}

					i++;
				}

				builder.setDescription("**Removing tracks from history:**\n" + bd);
				ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
			} else {
				ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no history to remove!");
			}
		}
	}

	@SubCommand( value = HistorySlashCommand.class)
	public static class clearHistory  implements ISlashCommand
	{
		@Override
		public String getDescription()
		{
			return "Fully clear your recent music history";
		}

		@Override
		public String commandName()
		{
			return "clear";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ArrayList<AudioObject> tracks = MusicCommand.getUserHistory(guild, author);
			MusicCommand.trackHistory.get(guild.getIdLong()).remove(author.getIdLong());

			int j = tracks.size();

			if (j > 0) {
				EmbedBuilder builder = new EmbedBuilder();
				StringBuilder bd = new StringBuilder();

				int i = 0;
				for (AudioObject track : tracks) {
					StringBuilder bd1 = new StringBuilder();

					String title = track.name;

					if (title.contains("[") && !title.contains("]")) {
						title += "]";
					}
					if (title.length() > 75) {
						title = title.substring(0, 75);
					}

					bd1.append("[**").append(title).append("**](").append(track.url).append(")");

					if (bd1.length() + bd.length() > (MessageEmbed.VALUE_MAX_LENGTH - 80)) {
						bd.append("And ").append(tracks.size() - i).append(" more");
						break;
					} else {
						bd.append(bd1).append("\n");
					}

					i++;
				}

				builder.setDescription("**Clearing tracks from history:**\n" + bd);
				ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
			} else {
				ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no history to clear!");
			}
		}
	}
}