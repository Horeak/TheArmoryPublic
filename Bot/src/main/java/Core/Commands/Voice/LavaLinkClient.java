package Core.Commands.Voice;

import Core.Commands.Voice.Objects.TrackObject;
import Core.Commands.Voice.Objects.TrackObject.Track;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.VariableState;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Events.BotCloseEvent;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class LavaLinkClient
{
	public static JdaLavalink lavalink;
	
	public static String password;
	public static String version;

	public static boolean init = false;

	//TODO Use the GetTracks function as a search function, will allow searching soundcloud for example
	
	//@VariableState( variable_class = "Core.Main.Startup", variable_name = "USE_LAVA_LINK" )
	@PreInit
	public static void initLavaLink()
	{
		if(!Startup.USE_LAVA_LINK) return;

		password = Startup.getEnvValue("lavalink:password");
		version = Startup.getEnvValue("lavalink:version");

		if(password == null || password.isBlank()){
			System.err.println("Unable to init LavaLink connection!");
			return;
		}

		String userid = Startup.debug ? "206178161023516672" : "188361942098771969";
		lavalink = new JdaLavalink(userid, 1, shardId -> Startup.getClient());
		lavalink.setUserId(userid);
		
		if (Startup.jarFile) {
			lavalink.addNode(URI.create("ws://lavalink"), password);
		}else{
			lavalink.addNode(URI.create("ws://" + Startup.getEnvValue("lavalink:ip")), password);
			
		}
		init = true;
	}

	@VariableState( variable_class = "Core.Main.Startup", variable_name = "USE_LAVA_LINK" )
	@EventListener
	public static void handle(BotCloseEvent event){
		lavalink.shutdown();
	}

	public static JdaLink getLink(Guild guild){
		return lavalink.getLink(guild);
	}
	
	private static final String[] SEARCH_TYPES = {"ytsearch", "spsearch", "amsearch", "scsearch"};
	
	public static ArrayList<Track> getTracks(String search){
		LinkedHashSet<Track> list = new LinkedHashSet<>();
		
		if (!search.startsWith("http://") && !search.startsWith("https://")) {
			for(String type : SEARCH_TYPES){
				list.addAll(LavaLinkClient.getTrackObject(type + ":" + search, false).stream().flatMap(s -> Arrays.stream(s.tracks)).toList());
			}
		}else{
			list.addAll(LavaLinkClient.getTrackObject(search, false).stream().flatMap(s -> Arrays.stream(s.tracks)).toList());
		}
		
		return new ArrayList<>(list);
	}
	
	public static ArrayList<TrackObject> getTrackObject(String identifier)
	{
		return getTrackObject(identifier, true);
	}
	
	public static ArrayList<TrackObject> getTrackObject(String identifier, boolean search) {
		ArrayList<TrackObject> list = new ArrayList<>();

		if(identifier == null || identifier.isBlank()){
			return list;
		}

		String id = URLEncoder.encode(identifier, StandardCharsets.UTF_8);
		
		if(search) {
			if (!id.startsWith("http://") && !id.startsWith("https://")) {
				id = "ytsearch:" + id;
			}
		}
		
		try {
			String url = Startup.jarFile ? "lavalink/" : ("http://" + Startup.getEnvValue("lavalink:ip") + "/");
			GetRequest t = Unirest.get(url + version + "/loadtracks?identifier=" + id).header("Authorization", password);
			JSONArray trackData = t.asJson().getBody().getArray();

			trackData.forEach(o -> {
				JSONObject object = ((JSONObject) o);
				TrackObject obj = Startup.getGSON().fromJson(object.toString(), TrackObject.class);

				if(obj != null){
					list.add(obj);
				}
			});

		} catch (UnirestException e) {
			Logging.exception(e);
		}

		return list;
	}

	public static boolean connectToChannel(VoiceChannel channel){
		Guild guild = channel.getGuild();
		JdaLink link = LavaLinkClient.getLink(guild);
		
		link.connect(channel);
		
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(false);
		return true;
	}

	public static boolean disconnectFromChannel(Guild guild){
		JdaLink link = LavaLinkClient.getLink(guild);
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(true);
		
		link.destroy();
		return true;
	}
}