package Core.CommandSystem.ComponentSystem;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.Component;

public class ComponentResponseObject
{
	final Long createdTime = System.currentTimeMillis();
	IDeferrableCallback parentEvent;
	
	final ComponentRunnable runnable;
	final Component component;
	final User eventAuthor;
	
	final boolean singleUse;
	final boolean authorOnly;
	
	public ComponentResponseObject(
			ComponentRunnable runnable, Component component, User eventAuthor, boolean singleUse, boolean authorOnly)
	{
		this.runnable = runnable;
		this.component = component;
		this.eventAuthor = eventAuthor;
		this.singleUse = singleUse;
		this.authorOnly = authorOnly;
	}
	
	@FunctionalInterface
	public interface ComponentRunnable
	{
		void run(IDeferrableCallback event);
	}
}

