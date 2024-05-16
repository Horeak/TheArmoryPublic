package Core.Commands.Voice;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.Events.QueueTrackEvent;
import Core.Commands.Voice.Objects.MusicTrackRunnable;
import Core.Commands.Voice.Objects.TrackData;
import Core.Commands.Voice.Objects.TrackObject;
import Core.Commands.Voice.Objects.TrackScheduler;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Annotation.Fields.GuildData;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MusicCommand implements ISlashCommand
{
	public static final int maxHistory = 10;
	@Save( "musicHistory.json")
	public static ConcurrentHashMap<Long, ConcurrentHashMap<Long, ArrayList<AudioObject>>> trackHistory = new ConcurrentHashMap<>();

	@GuildData
	@Save( "musicChannel.json")
	public static ConcurrentHashMap<Long, Long> musicChannel = new ConcurrentHashMap<>();
	//TODO Setting to disable voting for music commands or white/blacklist certain channels or roles?

	private static Map<Long, TrackScheduler> musicManagers;

	public static void queueSong(Guild guild,  BotChannel channel, User user, String url)
	{
		queueSong(guild, channel, user, url, null, true);
	}

	public static void queueSong(Guild guild,  BotChannel channel, User user, String url, MusicTrackRunnable trackLoaded)
	{
		queueSong(guild, channel, user, url, trackLoaded, true);
	}

	public static void queueSong(Guild guild, BotChannel channel, User user, String url, MusicTrackRunnable trackLoaded, boolean queue)
	{
		TrackScheduler musicManager = getGuildAudioPlayer(guild);
		ArrayList<TrackObject> objects = LavaLinkClient.getTrackObject(url);

		if (objects.size() > 0) {
			musicManager.queueSize += objects.size();
		}

		for (TrackObject object : objects) {
			for (TrackObject.Track tr : object.tracks) {
				AudioTrack track = null;

				try {
					track = LavalinkUtil.toAudioTrack(LavaLinkClient.lavalink.getAudioPlayerManager(), tr.track);
				} catch (IOException e) {
					Logging.exception(e);
				}

				if (track != null) {
					TrackData data = new TrackData(guild, channel, user, track.getInfo().author, track.getInfo().title);
					data.url = track.getInfo().uri;

					track.setUserData(data);

					if (queue) {
						musicManager.queue(track);
						Startup.getClient().getEventManager().handle(new QueueTrackEvent(Startup.getClient(), track));
					}
				}
			}

			musicManager.queueSize--;

			if (trackLoaded != null) {
				trackLoaded.run(object);
			}
		}
	}

	public static synchronized TrackScheduler getGuildAudioPlayer(Guild guild)
	{
		TrackScheduler musicManager;

		if (musicManagers.containsKey(guild.getIdLong())) {
			musicManager = musicManagers.get(guild.getIdLong());
		} else {
			musicManager = new TrackScheduler(guild);
			musicManagers.put(guild.getIdLong(), musicManager);
		}

		return musicManager;
	}

	@Init
	public static void init()
	{
		musicManagers = new HashMap<>();
	}

	public static void addToUserHistory( Guild guild,  User user, String url, String name, long duration, String channelId)
	{
		MusicCommand.getUserHistory(guild, user).add(new AudioObject(url, name, duration, channelId));

		while (MusicCommand.getUserHistory(guild, user).size() > maxHistory) {
			MusicCommand.getUserHistory(guild, user).remove(0);
		}
	}

	public static ArrayList<AudioObject> getUserHistory(
			 Guild guild, User user)
	{
		if (!getGuildHistory(guild).containsKey(user.getIdLong())) {
			getGuildHistory(guild).put(user.getIdLong(), new ArrayList<>());
		}

		return getGuildHistory(guild).get(user.getIdLong());
	}

	public static ConcurrentHashMap<Long, ArrayList<AudioObject>> getGuildHistory(Guild guild)
	{
		if (!trackHistory.containsKey(guild.getIdLong())) {
			trackHistory.put(guild.getIdLong(), new ConcurrentHashMap<>());
		}

		return trackHistory.get(guild.getIdLong());
	}

	public static class AudioObject
	{
		public final String url;
		public final String name;
		public final long duration;
		public final String channelId;

		public AudioObject(String url, String name, long duration, String channelId)
		{
			this.url = url;
			this.name = name;
			this.duration = duration;
			this.channelId = channelId;
		}
	}

	@Command
	public static class MusicInfoCommand extends MusicCommand
	{
		@Override
		public String getDescription()
		{
			return "Shows how to use the all music commands in the bot";
		}

		@Override
		public String commandName()
		{
			return "music";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			EmbedBuilder builder = new EmbedBuilder();

			builder.setTitle("Music system info");

			builder.addField("Playing Music",
			                 "To play music using the bot join a voice channel and use the ``/music play <url/search word>`` command, Doing so adds to the queue which can be viewed with ``/music queue [page]``",
			                 false);

			//TODO Fix VoiceLowering
			builder.addField("Queue commands",
					//			                 "``/music voicelowering <0-100%>`` allows setting the percentage the music volume is lowered when someone talks, 0 disables it.\n" +
					         "``/music repeat`` toggles repeat mode in the current queue",
					         false);

			builder.addField("Additional commands",
			                 "``/music pause`` Pause current queue\n``/music resume`` Resume paused song\n``/music skip`` Vote to skip current song\n``/music skipto <number>`` Vote to skip to specific song\n``/music goto <time example: 1h, 6m, 7s>`` Go to the specific time in the song\n``/music shuffle`` Shuffles the current queue\n``/music removeTrack`` Remove a specific track from the current queue\n``/music clear`` Vote to clear current queue\n``/music volume <0-150/up/down/max/mute>`` Change the volume of the bot\n``/music current`` Shows current song,\n``/music history [remove/clear]`` Shows the last 5 songs you have played in the current server\n``/music replay [number]`` Replay a song from your history\n``/music join [channel]`` Makes the bot join your current voice channel or the one specified\n``/music leave`` Makes the bot leave its current voice channel",
			                 false);
			
			builder.addField("Playlists",
			                 "The bot supports creating custom playlists, To create or select a playlist use ``/music playlist select <playlist name>`` Then you can use ``/music playlist <add/remove> <search word/url>`` to add songs to the selected playlist.\n\nTo view all your playlists use ``/music playlists`` to view all songs in selected playlist use ``/music playlist`` to delete a playlist use ``/music playlist delete <playlist>`` " + "to load a playlist into the current queue do ``/music select <playlist>``",
			                 false);

			ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
		}
	}
}