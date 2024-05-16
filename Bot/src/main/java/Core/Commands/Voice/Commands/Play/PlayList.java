package Core.Commands.Voice.Commands.Play;

import Core.CommandSystem.ChatMessageBuilder;
import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Voice.LavaLinkClient;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.Objects.TrackObject.Track;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.BotChannel;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static Core.Commands.Voice.Commands.Queue.CurrentQueue.getTimeString;

@Command
public class PlayList extends MusicCommand
{
	public static final int maxSize = 25;

	public static final String defaultPlayList = "General";

	@Save( "userPlayLists.json")
	public static ConcurrentHashMap<Long, ConcurrentHashMap<String, ArrayList<AudioObject>>> playLists = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<String, ArrayList<AudioObject>> getUserPlayListOb(User user)
	{
		if (!playLists.containsKey(user.getIdLong())) {
			playLists.put(user.getIdLong(), new ConcurrentHashMap<>());
		}

		if (!playLists.get(user.getIdLong()).containsKey(defaultPlayList)) {
			playLists.get(user.getIdLong()).put(defaultPlayList, new ArrayList<>());
		}

		return playLists.get(user.getIdLong());
	}

	public static List<String> getUserPlayLists(
			User user)
	{
		return new ArrayList<>(Collections.list(getUserPlayListOb(user).keys()));
	}

	public static ArrayList<AudioObject> getPlayList(User user, String name)
	{
		if (!getUserPlayListOb(user).containsKey(defaultPlayList)) {
			getUserPlayListOb(user).put(defaultPlayList, new ArrayList<>());
		}

		for (String key : getUserPlayLists(user)) {
			if (key.equalsIgnoreCase(name)) {
				name = key;
			}
		}

		return getUserPlayListOb(user).get(name);
	}

	public static void announce(BotChannel channel, Guild guild, User author, Message message, String playlist)
	{
		for (String key : getUserPlayLists(author)) {
			if (key.equalsIgnoreCase(playlist)) {
				playlist = key;
			}
		}

		ArrayList<AudioObject> objects = new ArrayList<>(getPlayList(author, playlist));

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Adding playlist to queue from `" + playlist + "`: ");

		int i = 1;
		StringBuilder bd = new StringBuilder();

		for (AudioObject track : objects) {
			if (i > 10) {
				break;
			}

			String name = track.name;

			StringBuilder bd1 = new StringBuilder();

			bd1.append("**").append(i).append("**. ").append(objects.size() >= 10 && i < 10 ? " " : "").append(
					"[**").append(name).append("**](").append(track.url).append(")");
			bd1.append(" (**").append(getTimeString(track.duration)).append("**)\n");

			if (bd.toString().length() + bd1.toString().length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
				break;
			}

			bd.append(bd1);
			i++;
		}

		if (objects.size() > i) {
			int g = objects.size() - i;

			if (g > 0) {
				bd.append("\n*And ").append(g).append(" more.*");
			}
		}

		long time = 0L;
		for (AudioObject track : objects) {
			time += track.duration;
		}

		if (objects.size() > 0) {
			String t = bd.toString();

			if (t.length() >= MessageEmbed.VALUE_MAX_LENGTH - 5) {
				t = t.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 5) + "...";
			}

			builder.addField("Tracks", t, false);
		}

		if (time > 0) {
			String t = "(**" + getTimeString(time) + "**)";

			if (t.length() >= MessageEmbed.VALUE_MAX_LENGTH - 5) {
				t = t.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 5) + "...";
			}

			builder.addField("Total duration", t, false);
		}

		ChatUtils.sendMessage(channel, builder.build());
	}

	@Override
	public String commandName()
	{
		return "playlist";
	}

	@Override
	public String getDescription(){
		return null;
	}

	@Override
	public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }

	@SubCommand( value = MusicInfoCommand.class)
	public static class queuePlayList extends MusicCommand {
		@Override
		public String commandName()
		{
			return "queue-playlist";
		}

		@Override
		public String getDescription(){
			return "Add the selected playlist to the queue";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ConcurrentHashMap<String, ArrayList<AudioObject>> playlists = getUserPlayListOb(author);

			UUID menuID = UUID.randomUUID();
			StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(menuID.toString()).setPlaceholder("Select which playlist").setRequiredRange(1, 25);

			int g = 0;
			for(String t : playlists.keySet()){
				if(g+1 >= maxSize) break;
				menuBuilder.addOptions(SelectOption.of(t, t));
				g++;
			}

			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed("Select which playlist to queue.");

			slashBuilder.addAction(ComponentResponseSystem.addComponent(menuID, author, slashEvent, menuBuilder.build(), (e) -> {
				StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;
				ArrayList<String> t = new ArrayList<>(event.getValues());

				ChatMessageBuilder builder = ChatUtils.createSlashMessage(author, channel);
				builder.withEmbed(ChatUtils.makeEmbed(author, guild, channel, "Are you sure you wish to queue " + t.size() + " playlist" + (t.size() > 1 ? "s" : "") + "?"));

				builder.addAction(ComponentResponseSystem.addComponent(author, Button.success("id", "Yes"), (event1 -> {

					for(String playlist : t){
						announce(channel, guild, author, message, playlist);
						for(AudioObject object : getPlayList(author, playlist)){
							queueSong(guild, channel, author, object.url);
						}
					}

					ChatUtils.sendEmbed(channel, "All the playlists you selected have now been queued.");
				})));

				builder.addAction(ComponentResponseSystem.addComponent(author, Button.danger("id", "No"), (event1 -> ChatUtils.sendEmbed(channel, "Okay, nothing has been queued."))));

				builder.send();
			}));

			slashBuilder.send();
		}
	}

	@SubCommand( value = PlayList.class)
	public static class createPlayList extends MusicCommand {
		@SlashArgument( name = "name", description = "The name of the playlist you wish to create", required = true )
		public String playlistName;

		@Override
		public String commandName()
		{
			return "new-playlist";
		}

		@Override
		public String getDescription(){
			return "Create a new playlist";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			if(getUserPlayListOb(author).containsKey(playlistName.toLowerCase())){
				ChatUtils.sendEmbed(channel, "You already have a playlist with that name!" );
				return;
			}

			getUserPlayListOb(author).put(playlistName.toLowerCase(), new ArrayList<>());
			ChatUtils.sendEmbed(channel, "A playlist with the name '" + playlistName.toLowerCase() + "' has now been created!");
		}
	}

	@SubCommand( value = PlayList.class)
	public static class addSongsToPlaylist extends MusicCommand {
		@SlashArgument( name = "search", description = "The search phrase to use for adding songs", required = true )
		public String searchPhrase;

		@Override
		public String commandName()
		{
			return "add-songs";
		}

		@Override
		public String getDescription(){
			return "Add a new song to an existing playlist";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ArrayList<Track> tracks = LavaLinkClient.getTracks(searchPhrase);
			
			if (tracks.size() > 10) {
				tracks = new ArrayList<>(tracks.subList(0, 10));
			}
			
			if (tracks.size() > 0) {
				EmbedBuilder builder = new EmbedBuilder();
				StringBuilder st = new StringBuilder();

				int i = 1;
				for (Track ur : tracks) {
					String name = ur.info.title;

					if (name.length() > 70) {
						name = name.substring(0, 70) + "...";
					}

					if (name.contains("[") && !name.contains("]")) {
						name += "]";
					}
					
					st.append("**").append(i).append("**").append(") ").append(tracks.size() >= 10 && i == 1 ? " " : "").append(tracks.size() >= 10 && i < 10 ? "  " : "").append("[**").append(name).append("**](").append(ur.info.uri).append(")").append("\n");

					i++;
				}

				builder.setDescription(st.toString());
				
				final ArrayList<Track> urls1 = new ArrayList<>(tracks);

				ArrayList<ItemComponent> actions = new ArrayList<>();

				UUID menuID = UUID.randomUUID();
				StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(menuID.toString()).setPlaceholder("Select song to add").setRequiredRange(1, 25);

				for (int g = 0; g < (Math.min(urls1.size(), 10)); g++) {
					menuBuilder.addOptions(SelectOption.of("Nr. " + (g + 1), urls1.get(g).info.uri).withDescription(Utils.limitString(urls1.get(g).info.title, 50)));
				}

				ArrayList<Track> finalUrls = tracks;
				actions.add(ComponentResponseSystem.addComponent(menuID, author, slashEvent, menuBuilder.build(), (e) -> {
					StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;

					ConcurrentHashMap<String, ArrayList<AudioObject>> playlists = getUserPlayListOb(author);

					UUID menuID1 = UUID.randomUUID();
					StringSelectMenu.Builder menuBuilder1 = StringSelectMenu.create(menuID1.toString()).setPlaceholder("Select which playlist").setRequiredRange(1, 1);

					int g = 0;
					for(String t : playlists.keySet()){
						if(g+1 >= maxSize) break;
						menuBuilder1.addOptions(SelectOption.of(t, t));
						g++;
					}

					SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
					slashBuilder.withEmbed("Select which playlist to add songs to.");

					slashBuilder.addAction(ComponentResponseSystem.addComponent(menuID1, author, slashEvent, menuBuilder1.build(), (e2) -> {
						StringSelectInteractionEvent event1 = (StringSelectInteractionEvent)e2;
						String val = event1.getValues().get(0);

						for(String songs : event.getValues()){
							String sng = songs.substring(songs.indexOf("%_%:") + "%_%:".length());
							for(Track result : finalUrls){
								if(result.info.uri.equals(sng)){
									getPlayList(author, val).add(new AudioObject(result.info.uri, result.info.title, result.info.length, result.info.author));
								}
							}
						}

						ChatUtils.sendEmbed(channel, event.getValues().size() + " song" + (event.getValues().size() > 1 ? "s" : "") + " have now been added to '" + val + "'");
					}));

					slashBuilder.send();
				}));

				builder.setTitle("Please select a any of the songs out of the following result using the below selection menu.");

				SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
				slashBuilder.withEmbed(builder);
				slashBuilder.withActions(actions);
				slashBuilder.send();
			} else {
				ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no search results for search word `" + searchPhrase + "`");
			}
		}
	}

	@SubCommand( value = PlayList.class)
	public static class deleteSongs extends MusicCommand {

		@Override
		public String commandName()
		{
			return "delete-songs";
		}

		@Override
		public String getDescription(){
			return "Remove songs from a playlist";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ConcurrentHashMap<String, ArrayList<AudioObject>> playlists = getUserPlayListOb(author);

			UUID menuID = UUID.randomUUID();
			StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(menuID.toString()).setPlaceholder("Select which playlist").setRequiredRange(1, 1);

			int g = 0;
			for(String t : playlists.keySet()){
				if(g+1 >= maxSize) break;
				menuBuilder.addOptions(SelectOption.of(t, t));
				g++;
			}

			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed("Select which playlist to modify.");

			slashBuilder.addAction(ComponentResponseSystem.addComponent(menuID, author, slashEvent, menuBuilder.build(), (e) -> {
				StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;
				String val = event.getValues().get(0);

				UUID secondMenuID = UUID.randomUUID();
				StringSelectMenu.Builder secondMenuBuilder = StringSelectMenu.create(secondMenuID.toString()).setPlaceholder("Select songs").setRequiredRange(1, 25);

				if(playlists.get(val).size() == 0){
					ChatUtils.sendEmbed(channel, "This playlist is already empty.");
					return;
				}

				int g1 = 0;
				for(AudioObject t : playlists.get(val)){
					if(g1+1 >= maxSize) break;
					secondMenuBuilder.addOptions(SelectOption.of(t.name, g1 + "%_%:" + t.url));
					g1++;
				}

				SlashMessageBuilder slashBuilder1 = ChatUtils.createSlashMessage(author, channel);
				slashBuilder1.withEmbed("Select which songs to remove from the playlist.");

				slashBuilder1.addAction(ComponentResponseSystem.addComponent(secondMenuID, author, slashEvent, secondMenuBuilder.build(), (e1) -> {
					StringSelectInteractionEvent event1 = (StringSelectInteractionEvent)e1;

					for (String val1 : event1.getValues()) {
						String sng = val1.substring(val1.indexOf("%_%:") + "%_%:".length());
						getPlayList(author, val).removeIf((c1) -> c1.url.equals(sng));
					}

					ChatUtils.sendEmbed(channel, "The specific songs have now been removed!");
				}));

				slashBuilder1.send();
			}));

			slashBuilder.send();
		}
	}

	@SubCommand( value = PlayList.class)
	public static class deletePlaylists extends MusicCommand {

		@Override
		public String commandName()
		{
			return "delete-playlist";
		}

		@Override
		public String getDescription(){
			return "Delete an entire playlist";
		}

		@Override
		public void onExecute(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			ConcurrentHashMap<String, ArrayList<AudioObject>> playlists = getUserPlayListOb(author);

			UUID menuID = UUID.randomUUID();
			StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(menuID.toString()).setPlaceholder("Select which playlist").setRequiredRange(1, 25);

			int g = 0;
			for(String t : playlists.keySet()){
				if(g+1 >= maxSize) break;
				menuBuilder.addOptions(SelectOption.of(t, t));
				g++;
			}

			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed("Select which playlist to remove.");

			slashBuilder.addAction(ComponentResponseSystem.addComponent(menuID, author, slashEvent, menuBuilder.build(), (e) -> {
				StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;
				ArrayList<String> t = new ArrayList<>(event.getValues());

				ChatMessageBuilder builder = ChatUtils.createSlashMessage(author, channel);
				builder.withEmbed(ChatUtils.makeEmbed(author, guild, channel, "Are you sure you wish to delete " + t.size() + " playlist" + (t.size() > 1 ? "s" : "") + "?"));

				builder.addAction(ComponentResponseSystem.addComponent(author, Button.success("id", "Yes"), (event1 -> {
					t.forEach(key -> getUserPlayListOb(author).remove(key));
					ChatUtils.sendEmbed(channel, "All the playlists you selected have now been removed.");
				})));

				builder.addAction(ComponentResponseSystem.addComponent(author, Button.danger("id", "No"), (event1 -> ChatUtils.sendEmbed(channel, "Okay, they are not being removed."))));

				builder.send();
			}));

			slashBuilder.send();
		}
	}
}