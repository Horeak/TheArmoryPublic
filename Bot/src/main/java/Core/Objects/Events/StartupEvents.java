package Core.Objects.Events;

import Core.Main.Startup;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Util.Utils;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StartupEvents
{
	@PostInit
	public static void startup()
	{
		System.out.println("Started \"" + Startup.getClient().getSelfUser().getName() + "\" successfully!");
		Runtime.getRuntime().addShutdownHook(new Thread(Startup::onBotClose));
		System.out.println("Startup init done.");
		System.out.println("System startup took: " + Utils.getUpTime());
	}
	
	@Init
	public static void initReady(){
		Startup.getClient().getEventManager().register(new ListenerAdapter()
		{
			@Override
			public void onReady(@NotNull ReadyEvent event)
			{
				readyEvent(event);
			}
		});
	}
	
	public static void readyEvent(ReadyEvent event){
		System.out.println("\"" + Startup.getClient().getSelfUser().getName() + "\" is now ready.");
		System.out.println("Found " + event.getGuildTotalCount() + " server(s), " + (event.getGuildTotalCount() - event.getGuildUnavailableCount()) + " / " + event.getGuildTotalCount() + " are available.");
		System.out.println("Full startup and loading took: " + Utils.getUpTime());
	}
}