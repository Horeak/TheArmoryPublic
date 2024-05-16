package Core.Data;

import Core.Main.Logging;
import Core.Objects.Annotation.Fields.GuildData;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Util.ReflectionUtils;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

//TODO Make a similar system for users when the bot is removed from the last server the user is in

public class GuildDataSystem
{
	private static List<Field> fields;
	
	@Init
	public static void init(){
		fields = ReflectionUtils.getFields(GuildData.class);
	}
	
	@EventListener
	public static void onGuildJoin(GuildJoinEvent event){
		//TODO Add data when joining a guild?
	}
	
	@EventListener
	public static void onGuildLeave(GuildLeaveEvent event){
		long guildId = event.getGuild().getIdLong();
		for (Field ob : fields) {
			if(Map.class.isAssignableFrom(ob.getType())){
				try {
					Map map = (Map)ob.get(null);
					
					if(map.containsKey(guildId)){
						map.remove(guildId);
						
					}else map.remove(Long.toString(guildId));
					
				} catch (IllegalAccessException e) {
					Logging.exception(e);
				}
			}
		}
	}
}
