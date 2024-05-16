package Core.Commands.Voice.Objects;

import Core.Commands.Voice.Events.EndTrackEvent;
import Core.Commands.Voice.Events.QueueTrackEvent;
import Core.Commands.Voice.Events.StartTrackEvent;
import Core.Commands.Voice.LavaLinkClient;
import Core.Main.Logging;
import Core.Main.Startup;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends PlayerEventListenerAdapter
{
	//TODO Save current song as a url when the bot closes and attempt to resume it when it starts up
	//TODO Lavalink has a resume feature for the currently playing song so need to use a UUID for that
	//TODO Then requeue the rest of the urls with the bots queue system. REMEBER TO TAKE INTO ACCOUNT URLQUEUE AND NOT QUEUE ALL OF THE URLS!
	
	private final BlockingQueue<AudioTrack> queue;
	public final BlockingQueue<QueueObject> urlQueue;
	
	public AudioTrack currentTrack = null;
	
	private final Guild guild;
	public boolean repeat = false;
	
	private IPlayer player;
	
	public int queueSize = 0;
	
	public TrackScheduler(Guild guild)
	{
		this.guild = guild;
		this.queue = new LinkedBlockingQueue<>();
		this.urlQueue = new LinkedBlockingQueue<>();
		
		player = getPlayer();
	}
	
	public IPlayer getPlayer(){
		JdaLink link = LavaLinkClient.getLink(guild);
		IPlayer player = link.getPlayer();
		
		if(this.player != player) {
			player.addListener(this);
			this.player = player;
			return player;
		}
		
		return player;
	}
	
	public void queue(AudioTrack track)
	{
		if(getPlayer().getPlayingTrack() != null && getPlayer().getTrackPosition() > 0) {
			queue.offer(track);
		}else{
			getPlayer().playTrack(track);
			currentTrack = track;
		}
	}
	
	//TODO This triggers twice?
	public void onTrackStart(IPlayer player, AudioTrack track)
	{
		Startup.getClient().getEventManager().handle(new StartTrackEvent(Startup.getClient(), track));
	}
	
	@Override
	public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason)
	{
		if(currentTrack != null) {
			AudioTrack aTrack = currentTrack;
			
			if (repeat) {
				AudioTrack tt = aTrack.makeClone();
				tt.setUserData(aTrack.getUserData());
				queue.add(tt);
			}
			
			Startup.getClient().getEventManager().handle(new EndTrackEvent(Startup.getClient(), aTrack, queue.peek()));
		}
		
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
	
	public void nextTrack()
	{
		if (queue != null && queue.peek() != null) {
			AudioTrack track = queue.poll();
			
			if (track != null) {
				AudioTrack tt = track.makeClone();
				tt.setUserData(track.getUserData());
				getPlayer().playTrack(tt);
				currentTrack = track;
				
				//TODO Make sure this always runs when a song is done
				if(urlQueue != null && urlQueue.peek() != null) {
					QueueObject url = urlQueue.poll();
					loadSong(url);
				}
				
			}else{
				currentTrack = null;
			}
		}
	}
	
	
	public void loadSong(QueueObject obj){
		ArrayList<TrackObject> objects = LavaLinkClient.getTrackObject(obj.url);
		
		if (objects.size() > 0) {
			queueSize += objects.size();
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
					TrackData data = new TrackData(obj.guild, obj.channel, obj.user, obj.channelId, obj.title);
					
					track.setUserData(data);
					
					queue(track);
					Startup.getClient().getEventManager().handle(new QueueTrackEvent(Startup.getClient(), track));
					
					queueSize--;
				}
			}
		}
	}
	
	@Override
	public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs)
	{
//		nextTrack();
		System.err.println("Track stuck!");
		player.setPaused(true);
	}
	
	@Override
	public void onTrackException(IPlayer player, AudioTrack track, Exception exception)
	{
		super.onTrackException(player, track, exception);
		Logging.exception(exception);
		player.setPaused(true);
	}
	
	public int getQueueSize(){
		return queue.size();
	}
	
	public void clear(){
		queue.clear();
		urlQueue.clear();
	}
	
	public boolean removeTrack(AudioTrack track){
		return queue.remove(track);
	}
	
	public boolean removeAll(List<AudioTrack> tracks){
		return queue.removeAll(tracks);
	}
	
	public ArrayList<AudioTrack> getQueue(){
		return new ArrayList<>(queue);
	}
	
	public AudioTrack poll(){
		return queue.poll();
	}
	
	public AudioTrack peek(){
		return queue.peek();
	}
	
	public boolean isDirty(){
		return queueSize > 0;
	}
}