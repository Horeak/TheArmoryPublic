package Core.Objects.Interfaces.Commands;

import Core.CommandSystem.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.SlashCommands.SlashCommandMessage;
import Core.Main.Logging;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public interface ISlashCommand extends IBaseSlashCommand
{
	UUID commandId = UUID.randomUUID();
	HashMap<UUID, Consumer<GenericComponentInteractionCreateEvent>> interacts = new HashMap<>();
	default OptionData[] commandOptions(){
		return new OptionData[0];
	}
	default DefaultMemberPermissions commandPrivileges(){
		return DefaultMemberPermissions.ENABLED;
	}
	void onExecute(@NonNull SlashCommandInteractionEvent slashEvent, Guild guild, @NonNull User author, @NonNull SlashCommandChannel channel, @NonNull SlashCommandMessage message);

	default ActionComponent prepareInteract(ActionComponent actionComponent, Consumer<GenericComponentInteractionCreateEvent> run){
		UUID btnId = UUID.randomUUID();
		String id = "interact:" + commandId + "_" + btnId;

		try{
			Field field = actionComponent.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(actionComponent, id);
		}catch(NoSuchFieldException | IllegalAccessException e){
			Logging.exception(e);
		}

		interacts.put(btnId, run);
		return actionComponent;
	}
}