package Core.CommandSystem.ComponentSystem;

import Core.CommandSystem.ComponentSystem.ComponentResponseObject.ComponentRunnable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentResponseSystem
{
	protected static final ConcurrentHashMap<UUID, ComponentResponseObject> objects = new ConcurrentHashMap<>();
	
	public static ItemComponent addComponent(User author, ItemComponent btn, ComponentRunnable command) {
		return addComponent(UUID.randomUUID(), author, null, btn, command, true, true);
	}
	
	public static ItemComponent addComponent(User author, IDeferrableCallback event, ItemComponent btn, ComponentRunnable command) {
		return addComponent(UUID.randomUUID(), author, event, btn, command, true, true);
	}
	
	public static ItemComponent addComponent(User author, ItemComponent btn, ComponentRunnable command, boolean singleUse, boolean authorOnly)
	{
		return addComponent(UUID.randomUUID(), author, null, btn, command, singleUse, authorOnly);
	}
	
	public static ItemComponent addComponent(User author, IDeferrableCallback event, ItemComponent btn, ComponentRunnable command, boolean singleUse, boolean authorOnly)
	{
		return addComponent(UUID.randomUUID(), author, event, btn, command, singleUse, authorOnly);
	}
	
	public static ItemComponent addComponent(UUID id, User author, ItemComponent btn, ComponentRunnable command) {
		return addComponent(id, author, null, btn, command, true, true);
	}
	
	public static ItemComponent addComponent(UUID id, User author, IDeferrableCallback event, ItemComponent btn, ComponentRunnable command)
	{
		return addComponent(id, author, event, btn, command, true, true);
	}
	
	public static ItemComponent addComponent(UUID id, User author, ItemComponent btn, ComponentRunnable command, boolean singleUse, boolean authorOnly)
	{
		return addComponent(id, author, null, btn, command, singleUse, authorOnly);
	}
	
	public static ItemComponent addComponent(UUID id, User author, IDeferrableCallback event, ItemComponent btn, ComponentRunnable command, boolean singleUse, boolean authorOnly)
	{
		ComponentResponseObject object = new ComponentResponseObject(command, btn, author, singleUse, authorOnly);
		object.parentEvent = event;
		
		if(btn instanceof Button){
			btn = Button.of(((Button)btn).getStyle(), id.toString(), ((Button)btn).getLabel());
		}
		
		objects.put(id, object);
		return btn;
	}
}
