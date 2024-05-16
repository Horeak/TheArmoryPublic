package Core.Commands.Voice.Commands.Play;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Commands.Queue.CurrentQueue;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Commands.Voice.Objects.TrackObject;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Util.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Collections;

@SubCommand( value = MusicInfoCommand.class)
public class Replay extends MusicCommand
{
	@SlashArgument( name = "count", description = "The amount of songs to go backwards" )
	public Integer songCount;

	@Override
	public String getDescription()
	{
		return "Replays a song from your recently played list";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channelX, SlashCommandMessage message)
	{
		VoiceChannel channel = ChatUtils.getConnectedBotChannel(guild);
		Member member = Utils.getMember(guild, author);

		if(member != null) {
			if (channel != null) {
				if (!channel.getMembers().contains(member)) {
					ChatUtils.sendEmbed(channelX, author.getAsMention() + " Please connect to the voice channel before trying to use this command!");
					return;
				}
			}
		}


		if (songCount < 0) {
			songCount = 0;
		}

		AudioObject object;
		ArrayList<AudioObject> objects = new ArrayList<>(MusicCommand.getUserHistory(guild, author));

		Collections.reverse(objects);

		if (objects.size() > songCount) {
			ArrayList<AudioTrack> tracks = new ArrayList<>();
			object = objects.get(songCount);

			if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() != null) {
				tracks.add(MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack());
				MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack().stop();
			}

			tracks.addAll(MusicCommand.getGuildAudioPlayer(guild).getQueue());

			MusicCommand.getGuildAudioPlayer(guild).clear();

			if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() != null) {
				MusicCommand.getGuildAudioPlayer(guild).getPlayer().stopTrack();
			}


			if (object != null) {
				queueSong(guild, channelX, author, object.url, trackObject -> {
					for(TrackObject.Track track : trackObject.tracks) {
						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle("Adding to queue: ");

						String tTime = track.info.length != null && track.info.length < System.currentTimeMillis() ? CurrentQueue.getTimeString(track.info.length) : "Unknown duration";
						builder.setDescription("[**" + track.info.title + "**](" + track.info.uri + ")" + " (**" + tTime + "**)");

						ChatUtils.sendMessage(channelX, builder.build());

						for (AudioTrack track1 : tracks) {
							MusicCommand.getGuildAudioPlayer(guild).queue(track1);
						}
					}
				});
			}
		} else {
			ChatUtils.sendEmbed(channelX,
			                    author.getAsMention() + " Found no song to replay" + (songCount != 0 ? " with that number!" : "!"));
		}
	}

	@Override
	public String commandName()
	{
		return "replay";
	}
}