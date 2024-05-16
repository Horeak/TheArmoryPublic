package Core.CommandSystem.SlashCommands;

import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

import java.util.List;

public class SlashCommandMessage extends ReceivedMessage
{
	public SlashCommandMessage(IDeferrableCallback event)
	{
		super(0L,
		      new SlashCommandChannel(event),
		      MessageType.DEFAULT,
		      null,
		      false,
		      0L,
		      false,
		      false,
		      null,
		      null,
		      event.getUser(),
		      event.getMember(),
		      null,
		      null,
		      null, List.of(),
		      List.of(),
		      List.of(),
		      List.of(),
		      List.of(),
		      0,
		      null,
		      null,
		      0);
	}
}