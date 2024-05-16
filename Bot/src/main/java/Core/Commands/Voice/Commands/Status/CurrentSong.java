package Core.Commands.Voice.Commands.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Commands.Queue.CurrentQueue;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Commands.Voice.Objects.TrackData;
import Core.Commands.Voice.Objects.TrackScheduler;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Util.Time.TimeParserUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SubCommand( value = MusicInfoCommand.class )
public class CurrentSong extends MusicCommand
{
	@Override
	public String getDescription()
	{
		return "Showing the currently playing song in this server";
	}

	public static EmbedBuilder getCurrentSongEmbed(
			 Guild guild,  User user, AudioTrack track, boolean automatic)
	{
		TrackScheduler manager = MusicCommand.getGuildAudioPlayer(guild);
		EmbedBuilder builder = new EmbedBuilder();

		builder.setThumbnail("https://png.icons8.com/metro/1600/music.png");
		builder.setTitle("Now playing: ");

		//TODO This doesnt get the right color?
		builder.setColor(ChatUtils.getEmbedColor(guild, user));

		StringBuilder desc = new StringBuilder();

		if(track.getUserData() != null && track.getUserData() instanceof TrackData data) {
			if (track.getInfo().title != null) {
				desc.append("[**").append(track.getInfo().title).append("**](").append(track.getInfo().uri).append(")");

				if (track.getInfo().author != null) {
					String author = track.getInfo().author;

					if (data.channelId != null) {
						if (!data.channelId.equalsIgnoreCase("unknown artist")) {
							author = "[" + author + "](" + "https://www.youtube.com/channel/" + URLEncoder.encode(data.channelId, StandardCharsets.UTF_8) + ")";
						}
					}

					desc.append("\nBy *").append(author).append("*");
				}
			}

			if (!automatic) {
				desc.append("\n\n**(").append(
						CurrentQueue.getTimeString(manager.getPlayer().getTrackPosition())).append(" / ").append(
						CurrentQueue.getTimeString(track.getDuration())).append(")**");
			} else {
				String t = track.getDuration() > System.currentTimeMillis() ? "Unknown duration" : TimeParserUtil.getTimeText(track.getDuration());
				desc.append("\n\n**Duration: ").append(t).append("**");
			}

			if (data.user != null) {
				builder.addField("Requested by", user.getAsMention(), true);
			}
		}

		builder.setDescription(desc.toString());

		return builder;
	}


	@Override
	public String commandName()
	{
		return "current";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (ChatUtils.getConnectedBotChannel(guild) == null) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " The bot is not connected to a voice channel!");
			return;
		}

		if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() != null) {
			AudioTrack track = MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack();

			if (track.getUserData() != null) {
				TrackData data = (TrackData)track.getUserData();

				EmbedBuilder ob = getCurrentSongEmbed(data.guild, data.user, track, false);
				
				ChatUtils.sendMessage(channel, ob.build());
			}

		} else {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " There is currently no music playing!");
		}
	}
}