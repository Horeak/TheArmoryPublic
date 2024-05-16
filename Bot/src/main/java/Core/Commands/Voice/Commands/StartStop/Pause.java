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
public class Pause extends MusicCommand
{
	@Override
	public String getDescription()
	{
		return "Pauses the current music queue";
	}


	@Override
	public String commandName()
	{
		return "pause";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().isPaused()) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Music is already paused!");
			return;
		}

		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(true);
		ChatUtils.sendEmbed(channel, author.getAsMention() + " Music is now paused!");
	}
}