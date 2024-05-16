package Core.Commands.Voice.Events;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

public class EndTrackEvent extends Event
{
	public final AudioTrack oldTrack;
	public final AudioTrack newTrack;
	
	public EndTrackEvent(
			JDA client, AudioTrack oldTrack, AudioTrack newTrack)
	{
		super(client);
		this.oldTrack = oldTrack;
		this.newTrack = newTrack;
	}
}
