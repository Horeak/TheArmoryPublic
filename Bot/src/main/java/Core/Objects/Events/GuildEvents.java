package Core.Objects.Events;

import Core.Objects.Annotation.Method.EventListener;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;

public class GuildEvents
{
	@EventListener
	public static void onGuildJoin(GuildJoinEvent event){
		System.out.println("The bot has now joined \"" + event.getGuild().getName() + "\" with " + event.getGuild().getMemberCount() + " members");
	}
	
	@EventListener
	public static void onGuildLeave(GuildLeaveEvent event){
		System.out.println("The bot has now been removed from \"" + event.getGuild().getName() + "\"");
	}
}
