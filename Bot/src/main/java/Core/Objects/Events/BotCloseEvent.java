package Core.Objects.Events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

//Event triggered when bot closes
public class BotCloseEvent extends Event
{
	public BotCloseEvent(@Nonnull JDA api)
	{
		super(api);
	}
}