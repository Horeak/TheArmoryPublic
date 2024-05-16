package Core.Commands.Voice.Objects;

import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class QueueObject extends TrackData
{
	public final String url;
	public Long length;
	
	public QueueObject(Guild guild, BotChannel channel, User user, String channelId, String title, String url)
	{
		super(guild, channel, user, channelId, title);
		this.url = url;
	}
}
