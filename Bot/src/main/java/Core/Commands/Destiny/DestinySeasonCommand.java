package Core.Commands.Destiny;


import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Destiny.Models.DestinySeasonObject;
import Core.Commands.Destiny.system.DestinySeasonSystem;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Command
public class DestinySeasonCommand implements ISlashCommand
{
	private static final DateFormat formatterTime = new SimpleDateFormat("MMMMM d, yyyy", Locale.US);

	@Override
	public String getDescription()
	{
		return "Shows some basic information about the current destiny 2 season.";
	}

	@Override
	public String commandName()
	{
		return "destiny-season";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		DestinySeasonObject currentSeason = null;
		DestinySeasonObject nextSeason = null;

		for(DestinySeasonObject season : DestinySeasonSystem.destinySeasons.values()){
			Date date = season.releaseDate;

			if(date.getTime() > System.currentTimeMillis()) {
				if(nextSeason == null || season.releaseDate.getTime() < nextSeason.releaseDate.getTime()){
					nextSeason = season;
				}

				continue;
			}

			if(currentSeason == null || season.releaseDate.getTime() > currentSeason.releaseDate.getTime()){
				currentSeason = season;
			}
		}

		EmbedBuilder builder = new EmbedBuilder();

		if(currentSeason != null){
			builder.setDescription("Season " + currentSeason.season + ": " + currentSeason.seasonName);
			if(nextSeason != null) {
				String timeLeft = TimeParserUtil.getTime(nextSeason.releaseDate.getTime());
				
				builder.addField("Times", "Start date\nEnd date\nTime left", true);
				builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "**" + formatterTime.format(currentSeason.releaseDate) + "**\n**" + formatterTime.format(
						nextSeason.releaseDate) + "**\n**" + timeLeft + "**", true);
			}else {
				builder.addField("Start date", formatterTime.format(currentSeason.releaseDate), true);
			}
		}

		builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, false);

		builder.addField("Power caps", "Minimum power\nSoft cap\nPowerful cap\nPinnacle cap", true);
		builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "**" + currentSeason.powerFloor + "**\n**" + currentSeason.softCap + "**\n**" + currentSeason.powerfulCap + "**\n**" + currentSeason.pinnacleCap + "**\n", true);

		ChatUtils.sendMessage(channel, null, builder.build());
	}
}