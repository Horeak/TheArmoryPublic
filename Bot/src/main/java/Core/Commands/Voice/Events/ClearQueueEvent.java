package Core.Commands.Voice.Events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;

public class ClearQueueEvent extends Event
{
	public final Guild guild;
	
	public ClearQueueEvent(
			JDA client, Guild guild)
	{
		super(client);
		this.guild = guild;
	}
}

