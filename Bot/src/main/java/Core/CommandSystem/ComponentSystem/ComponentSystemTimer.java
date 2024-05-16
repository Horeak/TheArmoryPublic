package Core.CommandSystem.ComponentSystem;

import Core.Objects.Annotation.Method.Interval;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ComponentSystemTimer
{
	public static final Long timeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);
	
	@Interval( time_interval = 1, initial_delay = 1 )
	public static void run()
	{
		for (Map.Entry<UUID, ComponentResponseObject> ent1 : ComponentResponseSystem.objects.entrySet()) {
			if (ent1.getValue() != null) {
				if (System.currentTimeMillis() >= (ent1.getValue().createdTime + timeout)) {
					ComponentResponseSystem.objects.remove(ent1.getKey());
					
					if (ent1.getValue().parentEvent != null) {
						ent1.getValue().parentEvent.getHook().editOriginalComponents(
								ActionRow.of(Button.danger("id", "Timed out").asDisabled())).queue();
					}
				}
			}
		}
	}
}
