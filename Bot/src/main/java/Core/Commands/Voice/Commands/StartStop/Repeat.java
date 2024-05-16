package Core.Commands.Voice.Commands.StartStop;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( value = MusicInfoCommand.class )
public class Repeat extends MusicCommand
{
	@Override
	public String getDescription()
	{
		return "Toggles repeat for the current music queue in this server";
	}

	@Override
	public String commandName()
	{
		return "repeat";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		boolean t = MusicCommand.getGuildAudioPlayer(guild).repeat;
		MusicCommand.getGuildAudioPlayer(guild).repeat = !t;
		ChatUtils.sendEmbed(channel, author.getAsMention() + " Queue repeat is now " + (!t ? "On" : "Off"));
	}
}