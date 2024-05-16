package Core.Commands.Voice.Commands.Queue;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Commands.Voice.Objects.QueueObject;
import Core.Objects.Annotation.Commands.SubCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Collections;

@SubCommand( value = MusicInfoCommand.class )
public class Shuffle extends MusicCommand
{
	@Override
	public String commandName()
	{
		return "shuffle";
	}

	@Override
	public String getDescription()
	{
		return "shuffle the current music queue";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ArrayList<AudioTrack> tracks = new ArrayList<>(MusicCommand.getGuildAudioPlayer(guild).getQueue());
		ArrayList<QueueObject> urlTracks = new ArrayList<>(MusicCommand.getGuildAudioPlayer(guild).urlQueue);

		Collections.shuffle(tracks);
		Collections.shuffle(urlTracks);

		MusicCommand.getGuildAudioPlayer(guild).clear();
		MusicCommand.getGuildAudioPlayer(guild).removeAll(tracks);
		MusicCommand.getGuildAudioPlayer(guild).clear();

		MusicCommand.getGuildAudioPlayer(guild).urlQueue.clear();

		tracks.forEach((t) -> MusicCommand.getGuildAudioPlayer(guild).queue(t));
		urlTracks.forEach((t) -> MusicCommand.getGuildAudioPlayer(guild).urlQueue.add(t));

		ChatUtils.sendEmbed(channel, "Music queue has been shuffled!");
	}
}