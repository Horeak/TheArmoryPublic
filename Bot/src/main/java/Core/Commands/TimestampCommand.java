package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

@Command
public class TimestampCommand implements ISlashCommand
{
	@SlashArgument( name = "time", description = "The time you want the format for. Default time zone is based on GMT", required = true )
	public String time;

	@Override
	public String commandName()
	{
		return "timestamp";
	}

	@Override
	public String getDescription(){
		return "Generate a discord timestamp from a given time";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		long timeMil = TimeParserUtil.getTime(author, time) + System.currentTimeMillis();
		
		StringBuilder builder2 = new StringBuilder();
		
		String builder1 = TimeFormat.TIME_SHORT.format(timeMil) + "\n" + TimeFormat.TIME_LONG.format(timeMil) + "\n" + TimeFormat.DATE_SHORT.format(timeMil) + "\n" + TimeFormat.DATE_LONG.format(timeMil) + "\n" + TimeFormat.DATE_TIME_SHORT.format(timeMil) + "\n" + TimeFormat.DATE_TIME_LONG.format(
				timeMil) + "\n" + TimeFormat.RELATIVE.format(timeMil) + "\n";

		builder2.append("`").append(TimeFormat.TIME_SHORT.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.TIME_LONG.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.DATE_SHORT.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.DATE_LONG.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.DATE_TIME_SHORT.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.DATE_TIME_LONG.format(timeMil)).append("`\n");
		builder2.append("`").append(TimeFormat.RELATIVE.format(timeMil)).append("`\n");

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Post this", builder2.toString(), true);
		embedBuilder.addField("Get this", builder1, true);

		ChatUtils.sendMessage(channel, embedBuilder.build());
	}
}