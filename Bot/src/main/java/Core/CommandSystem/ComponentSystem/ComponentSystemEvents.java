package Core.CommandSystem.ComponentSystem;

import Core.Objects.Annotation.Method.EventListener;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.UUID;
import java.util.regex.Pattern;

public class ComponentSystemEvents
{
	protected final static Pattern UUID_REGEX_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");
	
	@EventListener
	public static void SelectMenu(StringSelectInteractionEvent event){
		eventReceived(event);
	}
	
	@EventListener
	public static void buttonClicked(ButtonInteractionEvent event) {
		eventReceived(event);
	}
	
	protected static void eventReceived(GenericComponentInteractionCreateEvent event){
		String id = event.getComponentId();
		
		if(UUID_REGEX_PATTERN.matcher(id).matches()){
			UUID uuId = UUID.fromString(id);
			
			if (ComponentResponseSystem.objects.containsKey(uuId)) {
				ComponentResponseObject object = ComponentResponseSystem.objects.get(uuId);
				
				handleEvent(event, object, uuId);
			}
		}
	}
	
	protected static void handleEvent(GenericComponentInteractionCreateEvent event, ComponentResponseObject object, UUID id){
		if(event.getUser().getIdLong() != object.eventAuthor.getIdLong() && object.authorOnly) {
			event.reply("You are unable to respond to someone else's command. ").setEphemeral(true).queue();
			return;
		}
		
		event.deferEdit().queue();
		
		if(object.singleUse) {
			ItemComponent comp = object.component instanceof Button ? ((Button)object.component).asDisabled() : object.component instanceof SelectMenu ? ((SelectMenu)object.component).asDisabled() : null;
			
			//TODO Dont edit the message itself here only the components
			
			if (comp != null) {
				event.getHook().editOriginalComponents(ActionRow.of(comp)).queue();
			}
		}
		
		object.runnable.run(event);
		
		if(object.singleUse) {
			ComponentResponseSystem.objects.remove(id);
		}
	}
}