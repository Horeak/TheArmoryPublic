package Core.Commands.Destiny.User.Objects;

import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Destiny2RegisterObject
{
	public Long userId;
	public Long time;
	public Message message;
	public BotChannel channel;
	public SlashCommandInteractionEvent event;
}
