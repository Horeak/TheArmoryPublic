package Core.Objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import javax.annotation.Nonnull;

public class BotChannel implements MessageChannel
{
	private final MessageChannel channel;

	public BotChannel(MessageChannel channel)
	{
		this.channel = channel;
	}

	@Override
	public long getLatestMessageIdLong()
	{
		return channel.getLatestMessageIdLong();
	}

	@Override
	public boolean canTalk()
	{
		return channel != null && channel.canTalk();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return channel.getName();
	}

	@Nonnull
	@Override
	public ChannelType getType()
	{
		return channel.getType();
	}

	@Nonnull
	@Override
	public JDA getJDA()
	{
		return channel.getJDA();
	}

	
	@Override
	public RestAction<Void> delete()
	{
		return null;
	}

	@Nonnull
	@Override
	public MessageCreateAction sendMessage(@Nonnull CharSequence text)
	{
		if (getTextChannel() != null) {
			getTextChannel().sendMessage(text);
		}

		return channel.sendMessage(text);
	}


	
	@Override
	public MessageCreateAction sendMessageEmbeds( MessageEmbed embed,  MessageEmbed... other)
	{
		if (getTextChannel() != null) {
			getTextChannel().sendMessageEmbeds(embed);
		}

		return channel.sendMessageEmbeds(embed);
	}

	public TextChannel getTextChannel()
	{
		if (channel instanceof TextChannel) {
			return (TextChannel)channel;
		}

		return null;
	}

	public String getAsMention()
	{
		if (isPrivate()) {
			return null;
		}

		return "<#" + getIdLong() + '>';
	}

	public boolean isPrivate()
	{
		return getGuild() == null;
	}

	public Guild getGuild()
	{
		if (channel instanceof GuildChannel) {
			return ((GuildChannel)channel).getGuild();
		}
		
		return null;
	}

	@Override
	public long getIdLong()
	{
		return channel.getIdLong();
	}
}