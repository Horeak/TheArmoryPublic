package Core.Commands.Voice.Events;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

public class QueueTrackEvent extends Event
{
	public final AudioTrack track;
	
	public QueueTrackEvent(
			JDA client, AudioTrack track)
	{
		super(client);
		this.track = track;
	}
}
