package Core.Commands.Voice.Commands.StartStop;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( value = MusicInfoCommand.class )
public class Volume extends MusicCommand
{
	public static final int max = 150, min = 0;

	@SlashArgument( name = "volume", description = "The volume you want to bot to play at, 0-150%", required = true )
	public int targetVolume;

	@Override
	public String getDescription()
	{
		return "Sets the volume of the bot";
	}


	@Override
	public String commandName()
	{
		return "volume";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		targetVolume = Math.min(Math.max(targetVolume, min), max);

		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setVolume(targetVolume);
		ChatUtils.sendEmbed(channel,author.getAsMention() + " Volume is now " + (targetVolume + "%"));
	}
}