package Core.Commands.Voice.Commands.StartStop;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Events.SkipTrackEvent;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( value = MusicInfoCommand.class )
public class Skip extends MusicCommand
{
	@Override
	public String getDescription()
	{
		return "skip the currently playing song";
	}

	@Override
	public String commandName()
	{
		return "skip";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if((getGuildAudioPlayer(guild).currentTrack != null ? 1 : 0) + getGuildAudioPlayer(guild).getQueueSize() + getGuildAudioPlayer(guild).urlQueue.size() <= 0){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " The queue is currently empty and can not be skipped!");
			return;
		}

		ChatUtils.sendEmbed(channel, author.getAsMention() + " Skipping current song!");
		Startup.getClient().getEventManager().handle(new SkipTrackEvent(guild.getJDA(), getGuildAudioPlayer(guild).getPlayer().getPlayingTrack()));
		//		getGuildAudioPlayer(guild).getPlayer().getPlayingTrack().stop();
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().stopTrack();
		getGuildAudioPlayer(guild).nextTrack();
	}
}