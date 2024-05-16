package Core.CommandSystem.SlashCommands;

import Core.CommandSystem.ChatUtils;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.ArgumentAutoComplete;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.AutoComplete;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SlashCommandEvents{
	public static final Cache<UUID, ISlashCommand> command_instances = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.MINUTES).build();
	public static final Cache<Long, UUID> user_instances = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.MINUTES).build();
	
	@EventListener
	public static void slashCommandAutoComplete(CommandAutoCompleteInteractionEvent event){
		if(SlashCommandUtils.command_registry.containsKey(getCommandName(event))){
			
			try{
				ISlashCommand slashCommand = SlashCommandUtils.command_registry.get(getCommandName(event)).getClass().getDeclaredConstructor().newInstance();
				SlashCommandUtils.prepareCommandInstance(event.getInteraction(), slashCommand);

				command_instances.put(slashCommand.commandId, slashCommand);
				user_instances.put(event.getUser().getIdLong(), slashCommand.commandId);

				for(Field fe : slashCommand.getClass().getDeclaredFields()){
					if(fe.isAnnotationPresent(ArgumentAutoComplete.class)){
						ArgumentAutoComplete argument = fe.getAnnotation(ArgumentAutoComplete.class);

						if(Objects.equals(argument.value(), event.getFocusedOption().getName())){
							Object t = fe.get(slashCommand);

							if(t instanceof AutoComplete autoComplete){
								List<String> keys = autoComplete.value(event.getFocusedOption().getValue()).stream().limit(OptionData.MAX_CHOICES).toList();
								if(keys.size() > 0) {
									try {
										event.replyChoiceStrings(keys).queue();
									}catch (ErrorResponseException e){
										if(e.getErrorResponse() != ErrorResponse.UNKNOWN_INTERACTION) {
											Logging.exception(e);
										}
									}
								}else{
									event.replyChoiceStrings("No results found.").queue();
								}
							}
						}
					}
				}


			}catch(InstantiationException | IllegalAccessException e){
				Logging.exception(e);
			} catch (InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	private static String getCommandName(CommandInteractionPayload event){
		StringBuilder builder = new StringBuilder(event.getName());
		if (event.getSubcommandGroup() != null)
			builder.append('/').append(event.getSubcommandGroup());
		if (event.getSubcommandName() != null)
			builder.append('/').append(event.getSubcommandName());
		return builder.toString();
	}
	
	
	@EventListener
	public static void slashCommand(SlashCommandInteractionEvent event){
		String commandName = getCommandName(event);
		
		if(SlashCommandUtils.command_registry.containsKey(commandName)){
			
			try{
				if(event.getChannel() instanceof ThreadChannel channel){
					channel.join().queue();
				}

				UUID instanceId = user_instances.getIfPresent(event.getUser().getIdLong());
				ISlashCommand slashCommand = SlashCommandUtils.command_registry.get(getCommandName(event)).getClass().getDeclaredConstructor().newInstance();
				
				if(instanceId != null){
					ISlashCommand commandInstance = command_instances.getIfPresent(instanceId);
					
					if(commandInstance != null && Objects.equals(commandInstance.getClass(), slashCommand.getClass())){
						slashCommand = commandInstance;
					}
				}
				

				StringBuilder builder = new StringBuilder();

				if(!ChatUtils.isPrivate(event.getChannel()) && event.getGuild() != null){
					if(Startup.debug){
						builder.append("[").append(event.getGuild().getName()).append("/").append(event.getChannel().getName()).append("]");
					}else{
						builder.append("[").append(event.getGuild().getName()).append("] ");
					}
				}

				if(Startup.debug) {
					builder.append("[").append(event.getUser().getName()).append("] ");
				}
				
				builder.append("Slash command received: /").append(commandName);
				
				for(OptionMapping mapping : event.getOptions()){
					if(mapping != null){
						var value = !Startup.debug ? "****" : mapping.getAsString();
						builder.append(" ").append(mapping.getName()).append(": \"").append(value).append("\"");
					}
				}

				SlashCommandMessage message = new SlashCommandMessage(event);
				SlashCommandChannel channel = new SlashCommandChannel(event);

				SlashCommandUtils.prepareCommandInstance(event, slashCommand);

				System.out.println(builder);
				slashCommand.onExecute(event, event.getGuild(), event.getUser(), channel, message);
				
				command_instances.put(slashCommand.commandId, slashCommand);
				user_instances.put(event.getUser().getIdLong(), slashCommand.commandId);

			}catch(InstantiationException | IllegalAccessException e){
				Logging.exception(e);
			} catch (InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@EventListener
	public static void SelectMenu(StringSelectInteractionEvent event){
		eventReceived(event);
	}

	@EventListener
	public static void buttonClicked(ButtonInteractionEvent event){
		eventReceived(event);
	}

	protected static void eventReceived(GenericComponentInteractionCreateEvent event){
		
		try {
			String id = event.getComponentId();
			
			if(id.startsWith("interact:")){
				String string = id.replace("interact:", "");
				String[] ids = string.split("_");
				UUID commandId = UUID.fromString(ids[0]);
				UUID btnId = UUID.fromString(ids[1]);
				
				ISlashCommand cmd = command_instances.getIfPresent(commandId);
				
				if(cmd != null){
					if(cmd.interacts.containsKey(btnId)){
						cmd.interacts.get(btnId).accept(event);
					}
				}
			}
		}catch (Exception e){
			Logging.exception(e);
		}
	}
}