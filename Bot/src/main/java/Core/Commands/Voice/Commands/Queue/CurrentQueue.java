package Core.Commands.Voice.Commands.Queue;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Commands.Voice.Objects.QueueObject;
import Core.Commands.Voice.Objects.TrackData;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SubCommand( value = MusicInfoCommand.class )
public class CurrentQueue extends MusicCommand
{
	@SlashArgument( name = "page", description = "Which page of the queue you want to view." )
	public int page = 0;

	@Override
	public String getDescription()
	{
		return "Show the current queue for this server";
	}

	public static String getTimeString(Long time)
	{
		long hours = TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS);
		time -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);

		long minutes = TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS);
		time -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);

		long seconds = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);

		String text = "";

		if (hours > 0) {
			if (hours >= 10) {
				text = text + hours + ":";
			} else {
				text = text + "0" + hours + ":";
			}
		}

		if (minutes >= 10) {
			text = text + minutes + ":";
		} else {
			text = text + "0" + minutes + ":";
		}

		if (seconds >= 10) {
			text = text + seconds;
		} else {
			text = text + "0" + seconds;
		}

		return text;
	}

	@Override
	public String commandName()
	{
		return "queue";
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ArrayList<QueueObject> tracks = new ArrayList<>();

		for(AudioTrack track : MusicCommand.getGuildAudioPlayer(guild).getQueue()){
			if(track.getUserData() instanceof TrackData data){
				
				QueueObject object = new QueueObject(guild, channel, author, data.channelId, track.getInfo().title, track.getInfo().uri);
				tracks.add(object);
			}
		}

		tracks.addAll(MusicCommand.getGuildAudioPlayer(guild).urlQueue);

		double d = (double)tracks.size() / 10D;
		int maxPages = (int)Math.ceil(d);

		if (page >= maxPages) {
			page = maxPages - 1;
		}

		if (tracks.size() <= (maxPages * 10)) {
			maxPages -= 1;
		}

		if (maxPages <= 0) {
			page = 0;
			maxPages = 0;
		}

		StringBuilder builder = new StringBuilder();
		int maxNameLength = 40;

		if (tracks.size() > 0) {
			int max = 0;
			for (int i = 0; i < 10; i++) {
				int g = i + (page * 10);
				max = g;

				if (g < tracks.size()) {
					QueueObject track = tracks.get(g);

					String name = track.title != null ? track.title : "Error?";
					String user = null;

					if (track.user != null) {
						user = track.user.getName();
					}

					if (name.length() > maxNameLength) {
						name = name.substring(0, maxNameLength) + "...";
					}

					if (name.contains("[") && !name.contains("]")) {
						name += "]";
					}

					String tTime = track.length != null && track.length < System.currentTimeMillis() ? getTimeString(track.length) : "Unknown duration";
					String space2 = StringUtils.repeat(" ", (10 - tTime.length()));

					String preSpace = StringUtils.repeat(" ", (i != 9 ? 2 : 0));
					String space = StringUtils.repeat(" ", (maxNameLength + 5) - name.length());

					builder.append(g + 1).append(". ").append(i == 0 ? " " : "").append(preSpace).append("[**").append(
							name).append("**]").append("(").append(track.url).append(")").append(
							space).append("(").append(tTime).append(")").append(space2).append(
							user != null ? " [**" + user + "**]" : "").append("\n");
				}
			}
			int left = tracks.size() - (max + 1);

			if (left > 0) {
				builder.append("\n*And ").append(left).append(" more.*\n");
			}
		}

		StringBuilder desc = new StringBuilder();

		EmbedBuilder builder1 = new EmbedBuilder();
		builder1.setTitle("**Current queue: **");
		builder1.setFooter("Page: " + (page + 1) + " / " + (maxPages + 1), null);
		desc.append(builder);

		if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() != null) {
			AudioTrack track = MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack();
			String name = track.getInfo().title;
			String time = getTimeString(track.getDuration());

			if (name.length() > maxNameLength) {
				name = name.substring(0, maxNameLength) + "...";
			}

			if (name.contains("[") && !name.contains("]")) {
				name += "]";
			}
			
			builder1.addField("**Currently playing**",
			                  "[**" + name + "**](" + track.getInfo().uri + ")" + " ".repeat((50 - name.length())) + "(" + time + ")", false);
		}

		long time = 0L;

		for (QueueObject tt : tracks) {
			if(tt.length != null) {
				time += tt.length;
			}
		}

		int queueSize = tracks.size();

		if (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() != null) {
			time += MusicCommand.getGuildAudioPlayer(
					guild).getPlayer().getPlayingTrack().getDuration() - MusicCommand.getGuildAudioPlayer(
					guild).getPlayer().getTrackPosition();
			queueSize += 1;
		}

		String tTime = getTimeString(time);


		builder1.addField("**Queue time**", "**" + (!tTime.isEmpty() ? tTime : "N/A") + "**", true);
		builder1.addField("**Queue size**", "**" + queueSize + "**", true);
		if (ChatUtils.getConnectedBotChannel(guild) != null) {
			builder1.addField("**Playing in**", "`#" + ChatUtils.getConnectedBotChannel(guild).getName() + "`", false);
		}

		builder1.addField("**Status**", ChatUtils.getConnectedBotChannel(
				guild) == null ? "**Not in voice channel**" : MusicCommand.getGuildAudioPlayer(
				guild).getPlayer().isPaused() ? "**Paused**" : MusicCommand.getGuildAudioPlayer(
				guild).getPlayer().getPlayingTrack() == null ? "**No songs**" : "**Playing**", true);
		builder1.addField("**Repeat**",
		                  "**" + (MusicCommand.getGuildAudioPlayer(guild).repeat ? "On" : "Off") + "**",
		                  true);
		builder1.addField("**Volume**",
		                  "**" + (MusicCommand.getGuildAudioPlayer(guild).getPlayer().getVolume() + (" / 150%")) + "**",
		                  true);

		if (MusicCommand.getGuildAudioPlayer(guild).isDirty()) {
			desc.append("\n\n**Currently loading ").append(MusicCommand.getGuildAudioPlayer(guild).queueSize).append(
					" songs...**");
		}

		builder1.setDescription(desc.toString());

		ChatUtils.sendMessage(channel, builder1.build());
	}
}