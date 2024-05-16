package Core.Commands.Voice.Events;

import Core.CommandSystem.ChatMessageBuilder;
import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Voice.Commands.Play.Play;
import Core.Commands.Voice.Commands.Play.PlayList;
import Core.Commands.Voice.Commands.Status.CurrentSong;
import Core.Commands.Voice.LavaLinkClient;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.AudioObject;
import Core.Commands.Voice.Objects.TrackData;
import Core.Commands.Voice.Objects.TrackObject;
import Core.Commands.Voice.Objects.TrackObject.Track;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Method.EventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static Core.Commands.Voice.MusicCommand.getGuildAudioPlayer;

public class MusicEvents
{
	@EventListener
	public static void handle(QueueTrackEvent event)
	{
		if (event.track.getUserData() instanceof TrackData data) {
			
			if (data.guild != null && data.user != null) {

				if (ChatUtils.getConnectedBotChannel(data.guild) == null) {
					if (ChatUtils.getVoiceChannelFromUser(data.user, data.guild) != null) {
						VoiceChannel channelS = ChatUtils.getVoiceChannelFromUser(data.user, data.guild);

						LavaLinkClient.connectToChannel(channelS);
					}
				}
			}
		}
	}

	@EventListener
	public static void handle(StartTrackEvent event)
	{
		if (event.track.getUserData() instanceof TrackData data) {
			
			if (data.channel != null) {
				TextChannel channel = data.channel.getTextChannel();

				if (MusicCommand.musicChannel.containsKey(data.guild.getIdLong())) {
					TextChannel channel1 = Startup.getClient().getTextChannelById(MusicCommand.musicChannel.get(data.guild.getIdLong()));

					if (channel1 != null) {
						channel = channel1;
					}
				}
				//TODO Add a setting to disable the now playing message from being posted
				if (channel != null) {
					EmbedBuilder object = CurrentSong.getCurrentSongEmbed(data.guild, data.user, event.track, true);
					
					ChatMessageBuilder builder = ChatUtils.createMessage(data.user, channel);
					builder.withEmbed(object);
					
					builder.addAction(Button.danger("skip-" + data.url, "Skip"));
					builder.addAction(Button.success("favorite-" + data.url, "Favorite"));
					builder.addAction(Button.secondary("replay-" + data.url, "Replay"));
					
					builder.send();
				}
			}
		}
	}

	@EventListener
	public static void buttonEvent(ButtonInteractionEvent event){
		if(event.getButton().getId().startsWith("skip-")){
			String url = event.getButton().getId().substring("skip-".length());

			if(getGuildAudioPlayer(event.getGuild()).currentTrack != null) {
				if (getGuildAudioPlayer(event.getGuild()).currentTrack.getUserData() instanceof TrackData data) {
					
					if(data.url.equals(url)){
						Startup.getClient().getEventManager().handle(new SkipTrackEvent(event.getGuild().getJDA(), getGuildAudioPlayer(event.getGuild()).getPlayer().getPlayingTrack()));
						MusicCommand.getGuildAudioPlayer(event.getGuild()).getPlayer().stopTrack();
						getGuildAudioPlayer(event.getGuild()).nextTrack();

						event.reply(event.getUser().getAsMention() + " Skipping current song!").setEphemeral(true).queue();
						return;
					}
				}
				event.reply(event.getUser().getAsMention() + " This song has already ended!").setEphemeral(true).queue();
			}
		}else if(event.getButton().getId().startsWith("replay-")){
			String url = event.getButton().getId().substring("replay-".length());
			Play.queueUrl(event.getUser(), event.getGuild(), new SlashCommandChannel(event), MusicCommand.getGuildAudioPlayer(event.getGuild()), url);

		}else if(event.getButton().getId().startsWith("favorite-")){
			String url = event.getButton().getId().substring("favorite-".length());

			ConcurrentHashMap<String, ArrayList<AudioObject>> playlists = PlayList.getUserPlayListOb(event.getUser());

			UUID menuID1 = UUID.randomUUID();
			StringSelectMenu.Builder menuBuilder1 = StringSelectMenu.create(menuID1.toString()).setPlaceholder("Select which playlist").setRequiredRange(1, 1);

			int g = 0;
			for(String t : playlists.keySet()){
				if(g+1 >= PlayList.maxSize) break;
				menuBuilder1.addOptions(SelectOption.of(t, t));
				g++;
			}
			SlashCommandChannel sChannel = new SlashCommandChannel(event);
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(event.getUser(), sChannel);
			slashBuilder.withEmbed("Select which playlist to add the song to");

			slashBuilder.addAction(ComponentResponseSystem.addComponent(menuID1, event.getUser(), event, menuBuilder1.build(), (e2) -> {
				StringSelectInteractionEvent event1 = (StringSelectInteractionEvent)e2;
				String val = event1.getValues().get(0);
				ArrayList<TrackObject> objects = LavaLinkClient.getTrackObject(url);
				TrackObject object1 = objects.get(0);

				AudioTrack track = null;

				for(Track tr : object1.tracks) {
					try {
						track = LavalinkUtil.toAudioTrack(LavaLinkClient.lavalink.getAudioPlayerManager(), tr.track);

						if(track != null){
							break;
						}

					} catch (IOException e) {
						Logging.exception(e);
					}
				}

				PlayList.getPlayList(e2.getUser(), val).add(new AudioObject(track.getInfo().uri, track.getInfo().title, track.getInfo().length, null));
				event.getHook().sendMessageEmbeds(ChatUtils.makeEmbed(event.getUser(), event.getGuild(), sChannel, "You have now added 1 song to '" + val + "'").build()).setEphemeral(true).queue();
			}));

			slashBuilder.send();
		}
	}


	@EventListener
	public static void handle(ClearQueueEvent event)
	{
		if (event.guild != null) {
			if (getGuildAudioPlayer(event.guild).peek() == null) {
				if (getGuildAudioPlayer(event.guild).getPlayer().getPlayingTrack() == null) {
					if (ChatUtils.getConnectedBotChannel(event.guild) != null) {
						getGuildAudioPlayer(event.guild).repeat = false;

						LavaLinkClient.disconnectFromChannel(event.guild);
					}
				}
			}
		}
	}

	@EventListener
	public static void handle(EndTrackEvent event)
	{
		if (event.oldTrack != null && event.oldTrack.getUserData() != null) {
			TrackData data1 = (TrackData)event.oldTrack.getUserData();

			if (data1.guild != null && data1.user != null) {
				MusicCommand.addToUserHistory(data1.guild, data1.user, event.oldTrack.getInfo().uri, event.oldTrack.getInfo().title, event.oldTrack.getDuration(), event.oldTrack.getInfo().author);
			}
		}
		if (event.newTrack == null) {
			if (event.oldTrack.getUserData() instanceof TrackData data) {
				
				if (data.guild != null) {
					if (getGuildAudioPlayer(data.guild).peek() == null) {
						if (getGuildAudioPlayer(data.guild).getPlayer().getPlayingTrack() == null) {
							Startup.scheduledExecutor.schedule(() -> {
							if (ChatUtils.getConnectedBotChannel(data.guild) != null) {
								LavaLinkClient.disconnectFromChannel(data.guild);
							}
						}, 5, TimeUnit.SECONDS);
						}
					}
				}
			}
		}
	}
}